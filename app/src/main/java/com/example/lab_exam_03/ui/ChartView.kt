package com.example.lab_exam_03.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

enum class ChartType {
    LINE, AREA, BAR
}

class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    var chartType: ChartType = ChartType.LINE
    var primaryColor: Int = Color.parseColor("#22c55e")
    var goalColor: Int = Color.parseColor("#86efac")
    
    private var data: List<Double> = emptyList()
    private var goalValue: Double = 0.0
    
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    
    fun setData(newData: List<Double>, goal: Double = 0.0) {
        data = newData
        goalValue = goal
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (data.isEmpty()) {
            drawEmptyState(canvas)
            return
        }
        
        val padding = 40f
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2
        val maxValue = max(data.maxOrNull() ?: 0.0, goalValue).toFloat()
        
        if (maxValue <= 0) return
        
        when (chartType) {
            ChartType.LINE -> drawLineChart(canvas, padding, chartWidth, chartHeight, maxValue)
            ChartType.AREA -> drawAreaChart(canvas, padding, chartWidth, chartHeight, maxValue)
            ChartType.BAR -> drawBarChart(canvas, padding, chartWidth, chartHeight, maxValue)
        }
    }
    
    private fun drawEmptyState(canvas: Canvas) {
        paint.apply {
            color = Color.parseColor("#cccccc")
            textSize = 48f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("📊", width / 2f, height / 2f - 20f, paint)
        
        paint.textSize = 32f
        canvas.drawText("No data yet", width / 2f, height / 2f + 40f, paint)
    }
    
    private fun drawLineChart(canvas: Canvas, padding: Float, chartWidth: Float, chartHeight: Float, maxValue: Float) {
        val pointSpacing = if (data.size > 1) chartWidth / (data.size - 1) else chartWidth / 2
        
        // Draw goal line if applicable
        if (goalValue > 0) {
            val goalY = padding + chartHeight - (goalValue / maxValue * chartHeight).toFloat()
            paint.apply {
                color = goalColor
                strokeWidth = 4f
                pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
            }
            canvas.drawLine(padding, goalY, width - padding, goalY, paint)
            paint.pathEffect = null
        }
        
        // Draw line
        paint.apply {
            color = primaryColor
            strokeWidth = 6f
            style = Paint.Style.STROKE
        }
        
        path.reset()
        data.forEachIndexed { index, value ->
            val x = padding + index * pointSpacing
            val y = padding + chartHeight - (value / maxValue * chartHeight).toFloat()
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        canvas.drawPath(path, paint)
        
        // Draw points
        paint.style = Paint.Style.FILL
        data.forEachIndexed { index, value ->
            val x = padding + index * pointSpacing
            val y = padding + chartHeight - (value / maxValue * chartHeight).toFloat()
            canvas.drawCircle(x, y, 8f, paint)
        }
    }
    
    private fun drawAreaChart(canvas: Canvas, padding: Float, chartWidth: Float, chartHeight: Float, maxValue: Float) {
        val pointSpacing = if (data.size > 1) chartWidth / (data.size - 1) else chartWidth / 2
        
        // Draw area fill
        paint.apply {
            color = Color.argb(30, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor))
            style = Paint.Style.FILL
        }
        
        path.reset()
        path.moveTo(padding, padding + chartHeight)
        
        data.forEachIndexed { index, value ->
            val x = padding + index * pointSpacing
            val y = padding + chartHeight - (value / maxValue * chartHeight).toFloat()
            path.lineTo(x, y)
        }
        
        path.lineTo(width - padding, padding + chartHeight)
        path.close()
        canvas.drawPath(path, paint)
        
        // Draw line
        paint.apply {
            color = primaryColor
            strokeWidth = 6f
            style = Paint.Style.STROKE
        }
        
        path.reset()
        data.forEachIndexed { index, value ->
            val x = padding + index * pointSpacing
            val y = padding + chartHeight - (value / maxValue * chartHeight).toFloat()
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        canvas.drawPath(path, paint)
        
        // Draw points
        paint.style = Paint.Style.FILL
        data.forEachIndexed { index, value ->
            val x = padding + index * pointSpacing
            val y = padding + chartHeight - (value / maxValue * chartHeight).toFloat()
            canvas.drawCircle(x, y, 8f, paint)
        }
    }
    
    private fun drawBarChart(canvas: Canvas, padding: Float, chartWidth: Float, chartHeight: Float, maxValue: Float) {
        val barWidth = chartWidth / data.size * 0.7f
        val barSpacing = chartWidth / data.size
        
        // Draw goal line if applicable
        if (goalValue > 0) {
            val goalY = padding + chartHeight - (goalValue / maxValue * chartHeight).toFloat()
            paint.apply {
                color = goalColor
                strokeWidth = 4f
                pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
            }
            canvas.drawLine(padding, goalY, width - padding, goalY, paint)
            paint.pathEffect = null
        }
        
        // Draw bars
        paint.style = Paint.Style.FILL
        data.forEachIndexed { index, value ->
            val x = padding + index * barSpacing + (barSpacing - barWidth) / 2
            val barHeight = (value / maxValue * chartHeight).toFloat()
            val y = padding + chartHeight - barHeight
            
            paint.color = if (goalValue > 0 && value >= goalValue) primaryColor else goalColor
            canvas.drawRect(x, y, x + barWidth, padding + chartHeight, paint)
        }
    }
}

