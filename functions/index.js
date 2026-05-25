const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const logger = require("firebase-functions/logger");

const geminiApiKey = defineSecret("GEMINI_API_KEY");

const allowedCategories = [
  "육류",
  "해산물",
  "유제품",
  "채소",
  "과일",
  "음료",
  "냉동식품",
  "밀키트",
  "간편식",
  "가공식품",
  "곡류",
  "조미료",
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
      "사진 속 식품 또는 식품 포장 라벨을 분석해서 재고 등록 정보를 JSON으로만 반환해줘.",
      "식품이 여러 개 보이면 보이는 식품을 최대 10개까지 모두 추출해.",
      `카테고리는 반드시 다음 중 하나만 사용해: ${allowedCategories.join(", ")}.`,
      "해산물은 생선, 조개, 새우, 오징어 등 수산물을 포함해.",
      "밀키트는 조리 세트, 요리 키트, 찌개/전골/떡볶이 키트 등을 포함해.",
      "유통기한이 사진에 명확히 보이면 yyyy-MM-dd 형식으로 반환하고, 보이지 않으면 빈 문자열로 반환해.",
      "수량이 명확히 보이면 숫자 문자열로 반환하고, 보이지 않으면 빈 문자열로 반환해.",
      "반환 형식: {\"items\":[{\"itemName\":\"\",\"category\":\"\",\"quantity\":\"\",\"expirationDateText\":\"\"}]}",
    ].join("\n");

    const parsed = await callGeminiJson([
      { text: prompt },
      {
        inlineData: {
          mimeType,
          data: imageBase64,
        },
      },
    ]);

    const rawItems = Array.isArray(parsed.items) ? parsed.items : [parsed];
    const items = rawItems
      .map((item) => {
        const category = normalizeCategory(item.category);

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

exports.classifyFoodItems = onCall(
  {
    region: "asia-northeast3",
    secrets: [geminiApiKey],
    timeoutSeconds: 20,
    memory: "256MiB",
  },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "로그인이 필요합니다.");
    }

    const names = Array.isArray(request.data?.names)
      ? request.data.names
          .map((name) => String(name || "").trim())
          .filter(Boolean)
          .slice(0, 30)
      : [];

    if (names.length === 0) {
      throw new HttpsError("invalid-argument", "분류할 식품명이 필요합니다.");
    }

    const prompt = [
      "다음 식품명을 재고 카테고리로 분류해서 JSON으로만 반환해줘.",
      `카테고리는 반드시 다음 중 하나만 사용해: ${allowedCategories.join(", ")}.`,
      "해산물은 생선, 조개, 새우, 오징어 등 수산물을 포함해.",
      "밀키트는 조리 세트, 요리 키트, 찌개/전골/떡볶이 키트 등을 포함해.",
      "간편식은 도시락, 김밥, 즉석밥, 라면, 레토르트 식품 등을 포함해.",
      "애매하면 가장 가까운 식품 카테고리를 고르고, 정말 판단이 어려울 때만 기타를 사용해.",
      `식품명 목록: ${JSON.stringify(names)}`,
      "반환 형식: {\"items\":[{\"itemName\":\"원래 식품명\",\"category\":\"카테고리\"}]}",
    ].join("\n");

    const parsed = await callGeminiJson([{ text: prompt }], 512);
    const rawItems = Array.isArray(parsed.items) ? parsed.items : [];

    const items = names.map((name) => {
      const matched = rawItems.find(
        (item) => String(item.itemName || "").trim() === name
      );
      return {
        itemName: name,
        category: normalizeCategory(matched?.category),
      };
    });

    return { items };
  }
);

exports.generateInventoryTip = onCall(
  {
    region: "asia-northeast3",
    secrets: [geminiApiKey],
    timeoutSeconds: 20,
    memory: "256MiB",
  },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "로그인이 필요합니다.");
    }

    const items = Array.isArray(request.data?.items)
      ? request.data.items
          .map((item) => ({
            itemName: String(item?.itemName || "").trim(),
            category: normalizeCategory(item?.category),
            expireDate: String(item?.expireDate || "").trim(),
          }))
          .filter((item) => item.itemName)
          .slice(0, 20)
      : [];

    const prompt = [
      "사용자의 현재 재고를 보고 오늘의 식품 보관 팁을 JSON으로만 반환해줘.",
      "반드시 식품 보관 방법에 관한 팁만 작성해.",
      "레시피 추천, 소비 권장, 유통기한 임박 경고, 절약/낭비 관련 표현은 쓰지 마.",
      "재고 식품명이 있으면 그중 하나를 자연스럽게 포함해.",
      "팁은 한국어 한 문장, 45자 이내로 작성해.",
      "재고가 비어 있으면 일반적인 식품 보관 팁을 작성해.",
      `현재 재고: ${JSON.stringify(items)}`,
      "반환 형식: {\"tip\":\"\"}",
    ].join("\n");

    const parsed = await callGeminiJson([{ text: prompt }], 128);
    const tip = String(parsed.tip || "").trim();

    return {
      tip: tip.slice(0, 120),
    };
  }
);

async function callGeminiJson(parts, maxOutputTokens = 256) {
  const body = {
    contents: [
      {
        role: "user",
        parts,
      },
    ],
    generationConfig: {
      responseMimeType: "application/json",
      temperature: 0.1,
      maxOutputTokens,
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
    throw new HttpsError("internal", "Gemini API 요청에 실패했습니다.");
  }

  const json = await response.json();
  const text = json.candidates?.[0]?.content?.parts?.[0]?.text;

  if (!text) {
    throw new HttpsError("internal", "Gemini 분석 결과가 비어 있습니다.");
  }

  try {
    return JSON.parse(text);
  } catch (error) {
    logger.error("Failed to parse Gemini JSON", text, error);
    throw new HttpsError("internal", "Gemini 분석 결과를 해석하지 못했습니다.");
  }
}

function normalizeCategory(value) {
  const category = String(value || "").trim();
  return allowedCategories.includes(category) ? category : "기타";
}

function normalizeQuantity(value) {
  const quantityText = String(value || "").replace(/[^0-9]/g, "");
  if (!quantityText) return "";

  const quantity = Number.parseInt(quantityText, 10);
  return Number.isFinite(quantity) && quantity > 0 ? String(quantity) : "";
}

function normalizeDate(value) {
  const text = String(value || "").trim();
  return /^\d{4}-\d{2}-\d{2}$/.test(text) ? text : "";
}
