package com.example.java_video.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.java_video.ui.screens.create.AIAnchorScreen
import com.example.java_video.ui.screens.create.VoiceGenerationScreen
import com.example.java_video.ui.screens.create.VideoGenerationScreen
import com.example.java_video.viewmodel.VirtualPresenterViewModel

/**
 * 全新创作的作页面 - 三个主要功能卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(viewModel: VirtualPresenterViewModel) {
    var selectedFunction by remember { mutableStateOf<FunctionType?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // 页面标题区域
        CreateScreenHeader()

        // 主要功能卡片区域
        if (selectedFunction == null) {
            // 显示三个主要功能卡片
            MainFunctionCards(
                onFunctionSelected = { functionType ->
                    selectedFunction = functionType
                }
            )
        } else {
            // 显示选中的功能页面
            SelectedFunctionScreen(
                functionType = selectedFunction!!,
                viewModel = viewModel,
                onBack = { selectedFunction = null }
            )
        }
    }
}

/**
 * 创作页面头部标题
 */
@Composable
private fun CreateScreenHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "✨ 创作中心",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "选择您需要的创作工具，开始您的创意之旅",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        // 分隔线
        HorizontalDivider(
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .padding(top = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    }
}

/**
 * 主要功能卡片
 */
@Composable
private fun MainFunctionCards(
    onFunctionSelected: (FunctionType) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        FunctionType.values().forEach { functionType ->
            FunctionCard(
                functionType = functionType,
                onClick = { onFunctionSelected(functionType) }
            )
        }
    }
}

/**
 * 单个功能卡片
 */
@Composable
private fun FunctionCard(
    functionType: FunctionType,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() }
            .animateContentSize()
            .shadow(
                elevation = if (isHovered) 12.dp else 6.dp,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = functionType.primaryColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 左侧图标区域
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(functionType.primaryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = functionType.icon,
                    contentDescription = functionType.title,
                    modifier = Modifier.size(40.dp),
                    tint = functionType.primaryColor
                )
            }
            
            // 中间文字区域
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = functionType.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = functionType.primaryColor
                )
                
                Text(
                    text = functionType.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
                
                // 功能特点标签
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    functionType.features.take(2).forEach { feature ->
                        Surface(
                            modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                            color = functionType.primaryColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = feature,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = functionType.primaryColor,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
            
            // 右侧箭头
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "进入",
                modifier = Modifier.size(32.dp),
                tint = functionType.primaryColor.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 选中的功能页面
 */
@Composable
private fun SelectedFunctionScreen(
    functionType: FunctionType,
    viewModel: VirtualPresenterViewModel,
    onBack: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 返回按钮和标题
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = functionType.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = functionType.primaryColor
                )
                Text(
                    text = functionType.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 显示对应的功能页面
        when (functionType) {
            FunctionType.AI_ANCHOR -> {
                AIAnchorScreen(viewModel = viewModel)
            }
            FunctionType.VOICE_GENERATION -> {
                VoiceGenerationScreen(viewModel = viewModel)
            }
            FunctionType.VIDEO_GENERATION -> {
                VideoGenerationScreen(viewModel = viewModel)
            }
        }
    }
}

/**
 * 功能类型枚举
 */
enum class FunctionType(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val features: List<String>
) {
    AI_ANCHOR(
        title = "AI虚拟人播报",
        subtitle = "专业主播级播报体验",
        description = "利用AI技术生成专业的主播播报内容，适合新闻、资讯、产品介绍等场景",
        icon = Icons.Default.Person,
        primaryColor = Color(0xFF2196F3),
        features = listOf("专业播报", "多种风格", "智能合成")
    ),
    
    VOICE_GENERATION(
        title = "语音生成",
        subtitle = "文字转自然语音",
        description = "将文本内容转换为自然流畅的语音，支持多种情感和音色选择",
        icon = Icons.Default.RecordVoiceOver,
        primaryColor = Color(0xFF4CAF50),
        features = listOf("声音管理", "情感控制", "自定义音色")
    ),
    
    VIDEO_GENERATION(
        title = "视频生成",
        subtitle = "完整视频内容创作",
        description = "创建包含虚拟主播的完整视频作品，支持多种创作风格",
        icon = Icons.Default.VideoFile,
        primaryColor = Color(0xFF9C27B0),
        features = listOf("虚拟主播", "视频合成", "多种模式")
    )
}