package com.example.nurungji.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nurungji.R

@Composable
fun LoginScreen(
    onGoogleLoginClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFEFA),
                        Color(0xFFF5FBF2),
                        Color(0xFFE8F8EA)
                    ),
                    center = Offset(530f, 1120f),
                    radius = 980f
                )
            )
            .padding(horizontal = 24.dp)
    ) {
        BackgroundGlow(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 120.dp, y = 70.dp),
            size = 288.dp,
            color = Color(0xFF52B788)
        )
        BackgroundGlow(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-120).dp, y = (-80).dp),
            size = 384.dp,
            color = Color(0xFF8BD6A8)
        )
        BackgroundGlow(
            modifier = Modifier.align(Alignment.Center),
            size = 240.dp,
            color = Color(0xFFFFE5B4)
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .widthIn(max = 448.dp)
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LogoBox()

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "나만의 냉장고",
                fontSize = 48.sp,
                lineHeight = 52.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2F7657),
                textAlign = TextAlign.Center,
                letterSpacing = 0.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "1인 가구를 위한 스마트 냉장고 관리",
                fontSize = 18.sp,
                color = Color(0xFF697B69),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                letterSpacing = 0.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "냉장고 속 식재료를 놓치지 않고 관리하세요!",
                fontSize = 14.sp,
                color = Color(0xFF697B69),
                textAlign = TextAlign.Center,
                letterSpacing = 0.sp
            )

            Spacer(modifier = Modifier.height(64.dp))

            GoogleLoginButton(onClick = onGoogleLoginClick)

            Spacer(modifier = Modifier.height(24.dp))

            SafetyNote()

            Spacer(modifier = Modifier.height(64.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FeatureItem(
                    icon = Icons.Filled.Inventory2,
                    title = "재고 관리",
                    tint = Color(0xFFC78762)
                )
                FeatureItem(
                    icon = Icons.Filled.Search,
                    title = "레시피 추천",
                    tint = Color(0xFF5C448F)
                )
                FeatureItem(
                    icon = Icons.Filled.ShoppingCart,
                    title = "스마트 쇼핑",
                    tint = Color(0xFF6C8DCB)
                )
            }
        }
    }
}

@Composable
private fun LogoBox() {
    Box(
        modifier = Modifier
            .size(128.dp)
            .shadow(24.dp, RoundedCornerShape(40.dp), spotColor = Color(0x662B654E))
            .background(Color(0xFFC7ECC7), RoundedCornerShape(40.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(172.dp)
        )
    }
}

@Composable
private fun GoogleLoginButton(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = Color(0x443D5C45))
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 34.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            GoogleIcon()

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Google로 시작하기",
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF25352D),
                    letterSpacing = 0.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "간편하게 로그인하고 시작하세요",
                    fontSize = 12.sp,
                    color = Color(0xFF6C7A6D),
                    letterSpacing = 0.sp
                )
            }
        }
    }
}

@Composable
private fun SafetyNote() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(Color(0xFF58B98B), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(11.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "안전하고 빠른 로그인",
            fontSize = 12.sp,
            color = Color(0xFF738072),
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        )
    }
}

@Composable
private fun GoogleIcon() {
    Canvas(modifier = Modifier.size(40.dp)) {
        val stroke = Stroke(width = size.width * 0.11f, cap = StrokeCap.Round)
        val inset = size.width * 0.12f
        val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)

        drawArc(Color(0xFF4285F4), -40f, 95f, false, Offset(inset, inset), arcSize, style = stroke)
        drawArc(Color(0xFF34A853), 55f, 95f, false, Offset(inset, inset), arcSize, style = stroke)
        drawArc(Color(0xFFFBBC05), 150f, 72f, false, Offset(inset, inset), arcSize, style = stroke)
        drawArc(Color(0xFFEA4335), 222f, 88f, false, Offset(inset, inset), arcSize, style = stroke)
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(size.width * 0.54f, size.height * 0.52f),
            end = Offset(size.width * 0.82f, size.height * 0.52f),
            strokeWidth = size.width * 0.1f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    tint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0x33364D3B))
                .background(Color.White.copy(alpha = 0.68f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = title,
            fontSize = 12.sp,
            color = Color(0xFF6D7A70),
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        )
    }
}

@Composable
private fun BackgroundGlow(
    modifier: Modifier,
    size: Dp,
    color: Color
) {
    Box(
        modifier = modifier
            .size(size)
            .alpha(0.08f)
            .background(color, CircleShape)
    )
}
