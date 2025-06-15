package com.example.mytetris

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.BlurMaskFilter
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.mytetris.model.GameState
import kotlin.math.min

class TetrisView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    // 游戏状态
    private val gameState = GameState()
    
    // 画笔
    private val gridPaint = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    
    private val blockPaint = Paint().apply {
        style = Paint.Style.FILL
    }
    
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.LEFT
    }
    
    // 游戏区域参数
    private var cellSize = 0f
    private var gridLeft = 0f
    private var gridTop = 0f
    private var previewLeft = 0f
    private var previewTop = 0f
    
    // 触摸控制参数
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private val touchThreshold = 50f
    
    // 游戏循环
    private var lastUpdateTime = 0L
    private val gameLoop = object : Runnable {
        override fun run() {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime >= gameState.speed) {
                if (gameState.update()) {
                    invalidate()
                }
                lastUpdateTime = currentTime
            }
            postDelayed(this, 16) // 约60FPS
        }
    }
    
    // 虚拟按钮参数
    private val buttonPaint = Paint().apply {
        color = Color.parseColor("#80FFFFFF") // 半透明白色
        style = Paint.Style.FILL
    }
    
    private var buttonRadius = 150f  // 按钮半径
    private var buttonMargin = 20f   // 按钮间距
    private val buttonRect = RectF()
    
    // PlayStation 风格按钮绘制
    private val buttonShadowPaint = Paint().apply {
        color = Color.parseColor("#40000000")  // 半透明黑色阴影
        style = Paint.Style.FILL
        isAntiAlias = true
        maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
    }
    
    private val buttonBgPaint = Paint().apply {
        color = Color.WHITE  // 白色按钮
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val buttonHighlightPaint = Paint().apply {
        color = Color.parseColor("#40FFFFFF")  // 高光效果
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val symbolPaint = Paint().apply {
        color = Color.parseColor("#FF4081")  // 粉色符号
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    // 按钮位置（十字形布局）
    private var rotateButtonX = 0f   // 旋转按钮（上）
    private var rotateButtonY = 0f
    private var leftButtonX = 0f     // 左移按钮（左）
    private var leftButtonY = 0f
    private var rightButtonX = 0f    // 右移按钮（右）
    private var rightButtonY = 0f
    private var dropButtonX = 0f     // 快速下落按钮（下）
    private var dropButtonY = 0f
    
    private val buttonTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 90f  // 增大文字尺寸
        textAlign = Paint.Align.CENTER
    }
    
    init {
        // 开始游戏循环
        post(gameLoop)
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // 计算游戏区域大小
        val gridWidth = w * 0.6f // 游戏区域占60%宽度
        cellSize = gridWidth / 10 // 10列
        gridLeft = (w - gridWidth) / 2
        gridTop = h * 0.1f // 顶部留10%空间
        
        // 计算预览区域位置
        previewLeft = gridLeft + gridWidth + 20
        previewTop = gridTop
        
        // 计算按钮位置（十字形布局）
        val centerX = w / 2f
        val centerY = h * 0.85f  // 按钮区域在底部15%位置
        
        // 旋转按钮（上）
        rotateButtonX = centerX
        rotateButtonY = centerY - buttonRadius * 2 - buttonMargin
        
        // 左移按钮（左）
        leftButtonX = centerX - buttonRadius * 2 - buttonMargin
        leftButtonY = centerY
        
        // 右移按钮（右）
        rightButtonX = centerX + buttonRadius * 2 + buttonMargin
        rightButtonY = centerY
        
        // 快速下落按钮（下）
        dropButtonX = centerX
        dropButtonY = centerY + buttonRadius * 2 + buttonMargin
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // 绘制背景
        canvas.drawColor(Color.BLACK)
        
        // 绘制游戏网格
        drawGrid(canvas)
        
        // 绘制下一个方块预览
        drawNextTetromino(canvas)
        
        // 绘制分数
        drawScore(canvas)
        
        // 绘制游戏状态文本
        drawGameStatus(canvas)
        
        // 绘制虚拟按钮
        drawButtons(canvas)
    }
    
    private fun drawGrid(canvas: Canvas) {
        // 绘制网格背景
        canvas.drawRect(
            gridLeft - 2,
            gridTop - 2,
            gridLeft + cellSize * 10 + 2,
            gridTop + cellSize * 20 + 2,
            gridPaint
        )
        
        // 绘制网格线
        for (x in 0..10) {
            canvas.drawLine(
                gridLeft + x * cellSize,
                gridTop,
                gridLeft + x * cellSize,
                gridTop + cellSize * 20,
                gridPaint
            )
        }
        for (y in 0..20) {
            canvas.drawLine(
                gridLeft,
                gridTop + y * cellSize,
                gridLeft + cellSize * 10,
                gridTop + y * cellSize,
                gridPaint
            )
        }
        
        // 绘制方块
        val gridState = gameState.getGridState()
        for (y in gridState.indices) {
            for (x in gridState[y].indices) {
                gridState[y][x]?.let { color ->
                    blockPaint.color = color
                    canvas.drawRect(
                        gridLeft + x * cellSize + 1,
                        gridTop + y * cellSize + 1,
                        gridLeft + (x + 1) * cellSize - 1,
                        gridTop + (y + 1) * cellSize - 1,
                        blockPaint
                    )
                }
            }
        }
    }
    
    private fun drawNextTetromino(canvas: Canvas) {
        // 绘制预览区域背景
        canvas.drawRect(
            previewLeft - 2,
            previewTop - 2,
            previewLeft + cellSize * 4 + 2,
            previewTop + cellSize * 4 + 2,
            gridPaint
        )
        
        // 绘制"下一个"文本
        textPaint.textSize = 30f
        canvas.drawText("下一个", previewLeft, previewTop - 10, textPaint)
        
        // 绘制下一个方块
        val nextTetromino = gameState.getNextTetromino()
        val shape = nextTetromino.getCurrentShape()
        blockPaint.color = nextTetromino.color
        
        val blockSize = cellSize * 0.8f
        val offsetX = previewLeft + (4 - shape[0].size) * cellSize / 2
        val offsetY = previewTop + (4 - shape.size) * cellSize / 2
        
        for (y in shape.indices) {
            for (x in shape[y].indices) {
                if (shape[y][x]) {
                    canvas.drawRect(
                        offsetX + x * cellSize + (cellSize - blockSize) / 2,
                        offsetY + y * cellSize + (cellSize - blockSize) / 2,
                        offsetX + (x + 1) * cellSize - (cellSize - blockSize) / 2,
                        offsetY + (y + 1) * cellSize - (cellSize - blockSize) / 2,
                        blockPaint
                    )
                }
            }
        }
    }
    
    private fun drawScore(canvas: Canvas) {
        textPaint.textSize = 40f
        canvas.drawText(
            "分数: ${gameState.getScore()}",
            previewLeft,
            previewTop + cellSize * 6,
            textPaint
        )
    }
    
    private fun drawGameStatus(canvas: Canvas) {
        textPaint.textSize = 50f
        textPaint.textAlign = Paint.Align.CENTER
        val centerX = width / 2f
        val centerY = height / 2f
        
        when {
            !gameState.isStarted -> {
                canvas.drawText("点击开始游戏", centerX, centerY, textPaint)
            }
            gameState.isPaused -> {
                canvas.drawText("游戏暂停", centerX, centerY, textPaint)
            }
            gameState.isGameOver() -> {
                canvas.drawText("游戏结束", centerX, centerY, textPaint)
                textPaint.textSize = 30f
                canvas.drawText("点击重新开始", centerX, centerY + 50, textPaint)
            }
        }
        textPaint.textAlign = Paint.Align.LEFT
    }
    
    private fun drawButtons(canvas: Canvas) {
        // 绘制按钮阴影
        canvas.drawCircle(rotateButtonX, rotateButtonY + 5f, buttonRadius, buttonShadowPaint)
        canvas.drawCircle(leftButtonX, leftButtonY + 5f, buttonRadius, buttonShadowPaint)
        canvas.drawCircle(rightButtonX, rightButtonY + 5f, buttonRadius, buttonShadowPaint)
        canvas.drawCircle(dropButtonX, dropButtonY + 5f, buttonRadius, buttonShadowPaint)
        
        // 绘制旋转按钮（顶部）
        canvas.drawCircle(rotateButtonX, rotateButtonY, buttonRadius, buttonBgPaint)
        // 绘制按钮高光
        canvas.drawCircle(rotateButtonX - buttonRadius * 0.3f, rotateButtonY - buttonRadius * 0.3f, 
            buttonRadius * 0.4f, buttonHighlightPaint)
        // 绘制旋转符号（圆形箭头）
        val rotateSize = buttonRadius * 0.6f
        canvas.save()
        canvas.translate(rotateButtonX, rotateButtonY)
        // 绘制圆形箭头
        val rotatePath = Path()
        rotatePath.addArc(-rotateSize/2, -rotateSize/2, rotateSize/2, rotateSize/2, 0f, 270f)
        rotatePath.lineTo(rotateSize/4, -rotateSize/4)
        rotatePath.lineTo(rotateSize/2, 0f)
        rotatePath.close()
        canvas.drawPath(rotatePath, symbolPaint)
        canvas.restore()
        
        // 绘制左移按钮（任天堂风格十字键左）
        canvas.drawCircle(leftButtonX, leftButtonY, buttonRadius, buttonBgPaint)
        canvas.drawCircle(leftButtonX - buttonRadius * 0.3f, leftButtonY - buttonRadius * 0.3f, 
            buttonRadius * 0.4f, buttonHighlightPaint)
        // 绘制左箭头（任天堂风格）
        val arrowSize = buttonRadius * 0.5f
        val path = Path()
        path.moveTo(-arrowSize/2, 0f)  // 左尖
        path.lineTo(arrowSize/2, -arrowSize/2)  // 右上
        path.lineTo(arrowSize/2, arrowSize/2)   // 右下
        path.close()
        canvas.save()
        canvas.translate(leftButtonX, leftButtonY)
        canvas.drawPath(path, symbolPaint)
        canvas.restore()
        
        // 绘制右移按钮（任天堂风格十字键右）
        canvas.drawCircle(rightButtonX, rightButtonY, buttonRadius, buttonBgPaint)
        canvas.drawCircle(rightButtonX - buttonRadius * 0.3f, rightButtonY - buttonRadius * 0.3f, 
            buttonRadius * 0.4f, buttonHighlightPaint)
        canvas.save()
        canvas.translate(rightButtonX, rightButtonY)
        canvas.scale(-1f, 1f)  // 水平翻转
        canvas.drawPath(path, symbolPaint)
        canvas.restore()
        
        // 绘制快速下落按钮（任天堂风格十字键下）
        canvas.drawCircle(dropButtonX, dropButtonY, buttonRadius, buttonBgPaint)
        canvas.drawCircle(dropButtonX - buttonRadius * 0.3f, dropButtonY - buttonRadius * 0.3f, 
            buttonRadius * 0.4f, buttonHighlightPaint)
        canvas.save()
        canvas.translate(dropButtonX, dropButtonY)
        canvas.rotate(90f)  // 旋转90度
        canvas.drawPath(path, symbolPaint)
        canvas.restore()
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                
                // 处理游戏开始/重新开始
                if (!gameState.isStarted || gameState.isGameOver()) {
                    if (isInGameArea(event.x, event.y)) {
                        gameState.start()
                        invalidate()
                    }
                    return true
                }
                
                // 处理游戏暂停/继续
                if (event.x > width - 100 && event.y < 100) {
                    gameState.togglePause()
                    invalidate()
                    return true
                }
                
                // 处理虚拟按钮点击
                if (!gameState.isPaused && gameState.isStarted && !gameState.isGameOver()) {
                    when {
                        isInButton(event.x, event.y, rotateButtonX, rotateButtonY) -> {
                            gameState.rotate()
                            invalidate()
                        }
                        isInButton(event.x, event.y, leftButtonX, leftButtonY) -> {
                            gameState.move(-1, 0)
                            invalidate()
                        }
                        isInButton(event.x, event.y, rightButtonX, rightButtonY) -> {
                            gameState.move(1, 0)
                            invalidate()
                        }
                        isInButton(event.x, event.y, dropButtonX, dropButtonY) -> {
                            while (gameState.move(0, 1)) {
                                // 持续下落直到不能下落为止
                            }
                            invalidate()
                        }
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!gameState.isStarted || gameState.isPaused || gameState.isGameOver()) {
                    return true
                }
                
                // 处理滑动操作（保留原有的滑动控制逻辑）
                val dx = event.x - lastTouchX
                val dy = event.y - lastTouchY
                
                if (Math.abs(dx) > touchThreshold) {
                    gameState.move(if (dx > 0) 1 else -1, 0)
                    lastTouchX = event.x
                    invalidate()
                }
                
                if (dy > touchThreshold) {
                    gameState.move(0, 1)
                    lastTouchY = event.y
                    invalidate()
                }
            }
        }
        return true
    }
    
    private fun isInButton(x: Float, y: Float, buttonX: Float, buttonY: Float): Boolean {
        val dx = x - buttonX
        val dy = y - buttonY
        return dx * dx + dy * dy <= buttonRadius * buttonRadius
    }
    
    private fun isInGameArea(x: Float, y: Float): Boolean {
        return x >= gridLeft && x <= gridLeft + cellSize * 10 &&
               y >= gridTop && y <= gridTop + cellSize * 20
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(gameLoop)
    }
}