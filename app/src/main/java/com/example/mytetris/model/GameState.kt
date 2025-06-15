package com.example.mytetris.model

/**
 * 游戏状态管理类
 */
class GameState {
    // 游戏网格
    private val grid = Grid()
    
    // 游戏是否暂停
    var isPaused = false
        private set
    
    // 游戏是否开始
    var isStarted = false
        private set
    
    // 游戏速度（毫秒/帧）
    var speed = 1000L
        private set
    
    // 游戏开始
    fun start() {
        grid.start()
        isStarted = true
        isPaused = false
    }
    
    // 游戏暂停/继续
    fun togglePause() {
        if (isStarted && !grid.isGameOver) {
            isPaused = !isPaused
        }
    }
    
    // 游戏重置
    fun reset() {
        grid.start()
        isStarted = false
        isPaused = false
        speed = 1000L
    }
    
    // 更新游戏状态
    fun update(): Boolean {
        if (!isStarted || isPaused || grid.isGameOver) {
            return false
        }
        
        // 自动下落
        grid.move(0, 1)
        
        // 根据分数调整速度
        updateSpeed()
        
        return true
    }
    
    // 根据分数调整游戏速度
    private fun updateSpeed() {
        speed = when {
            grid.score >= 5000 -> 100L  // 最快速度
            grid.score >= 3000 -> 200L
            grid.score >= 1000 -> 400L
            grid.score >= 500 -> 600L
            else -> 1000L  // 初始速度
        }
    }
    
    // 获取当前分数
    fun getScore(): Int = grid.score
    
    // 获取游戏是否结束
    fun isGameOver(): Boolean = grid.isGameOver
    
    // 获取网格状态
    fun getGridState(): Array<Array<Int?>> = grid.getGridState()
    
    // 获取下一个方块
    fun getNextTetromino(): Tetromino = grid.getNextTetromino()
    
    // 移动方块
    fun move(dx: Int, dy: Int): Boolean {
        if (!isStarted || isPaused || grid.isGameOver) {
            return false
        }
        return grid.move(dx, dy)
    }
    
    // 旋转方块
    fun rotate(): Boolean {
        if (!isStarted || isPaused || grid.isGameOver) {
            return false
        }
        return grid.rotate()
    }
} 