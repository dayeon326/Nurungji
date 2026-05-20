const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const logger = require("firebase-functions/logger");

const geminiApiKey = defineSecret("GEMINI_API_KEY");

const allowedCategories = [
  "육류",
  "유제품",
  "채소",
  "과일",
  "음료",
  "냉동식품",
  "기타",
];

exports.analyzeFoodPhoto = onCall(
  {
    region: "asia-northeast3",
    secrets: [geminiApiKey],
    timeoutSeconds: 30,
    memory: "512MiB",
  },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "로그인이 필요합니다.");
    }

    const imageBase64 = request.data?.imageBase64;
    const mimeType = request.data?.mimeType || "image/jpeg";

    if (!imageBase64 || typeof imageBase64 !== "string") {
      throw new HttpsError("invalid-argument", "imageBase64가 필요합니다.");
    }

    const prompt = [
      "사진 속 식품 또는 식품 포장 라벨을 분석해 재고 등록 정보를 JSON으로만 반환해줘.",
      "식품이 여러 개 보이면 보이는 식품을 최대 10개까지 모두 추출해.",
      "카테고리는 반드시 다음 중 하나만 사용해: 육류, 유제품, 채소, 과일, 음료, 냉동식품, 기타.",
      "유통기한이 사진에 명확히 보이면 yyyy-MM-dd 형식으로 반환하고, 보이지 않으면 빈 문자열로 반환해.",
      "수량이 명확히 보이면 숫자 문자열로 반환하고, 보이지 않으면 \"1\"로 반환해.",
      "반환 형식: {\"items\":[{\"itemName\":\"\",\"category\":\"\",\"quantity\":\"1\",\"expirationDateText\":\"\"}]}",
    ].join("\n");

    const body = {
      contents: [
        {
          role: "user",
          parts: [
            { text: prompt },
            {
              inlineData: {
                mimeType,
                data: imageBase64,
              },
            },
          ],
        },
      ],
      generationConfig: {
        responseMimeType: "application/json",
        temperature: 0.1,
        maxOutputTokens: 256,
      },
    };

    const url =
      "https://generativelanguage.googleapis.com/v1beta/models/" +
      "gemini-3.1-flash-lite:generateContent?key=" +
      encodeURIComponent(geminiApiKey.value());

    const response = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const errorText = await response.text();
      logger.error("Gemini request failed", response.status, errorText);
      throw new HttpsError("internal", "사진 분석 API 요청에 실패했습니다.");
    }

    const json = await response.json();
    const text = json.candidates?.[0]?.content?.parts?.[0]?.text;

    if (!text) {
      throw new HttpsError("internal", "사진 분석 결과가 비어 있습니다.");
    }

    let parsed;
    try {
      parsed = JSON.parse(text);
    } catch (error) {
      logger.error("Failed to parse Gemini JSON", text, error);
      throw new HttpsError("internal", "사진 분석 결과를 해석하지 못했습니다.");
    }

    const rawItems = Array.isArray(parsed.items) ? parsed.items : [parsed];
    const items = rawItems
      .map((item) => {
        const category = allowedCategories.includes(item.category)
          ? item.category
          : "기타";

        return {
          itemName: String(item.itemName || "").trim(),
          category,
          quantity: normalizeQuantity(item.quantity),
          expirationDateText: normalizeDate(item.expirationDateText),
        };
      })
      .filter((item) => item.itemName)
      .slice(0, 10);

    return { items };
  }
);

function normalizeQuantity(value) {
  const quantity = Number.parseInt(String(value || "1").replace(/[^0-9]/g, ""), 10);
  return Number.isFinite(quantity) && quantity > 0 ? String(quantity) : "1";
}

function normalizeDate(value) {
  const text = String(value || "").trim();
  return /^\d{4}-\d{2}-\d{2}$/.test(text) ? text : "";
}
