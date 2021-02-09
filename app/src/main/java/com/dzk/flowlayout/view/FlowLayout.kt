package com.dzk.flowlayout.view

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import kotlin.math.max

class FlowLayout(context: Context) : ViewGroup(context) {
    private val mHorizontalSpacing: Int = dp2px(16)//每个item的横向间距
    private val mVerticalSpacing: Int = dp2px(10)//每个item的纵向间距
    //记录所有的行，一行行存储，用于layout
    private val allLine: ArrayList<ArrayList<View>> = ArrayList<ArrayList<View>>()
    //记录每一行行高，用于layout
    private val lineHeights:ArrayList<Int> = ArrayList()
    //反射构造
    constructor(context: Context, attributeSet: AttributeSet):this(context)
    constructor(context: Context,attributeSet: AttributeSet,defStyleAttr:Int):this(context,attributeSet)
    private fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    private fun clearMeasuredParams() {
        allLine.clear()
        lineHeights.clear()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        clearMeasuredParams()//避免内存抖动
        val count: Int = childCount
        val selfWidth = MeasureSpec.getSize(widthMeasureSpec)//ViewGroup给我的宽度
        val selfHeight = MeasureSpec.getSize(heightMeasureSpec)// ViewGroup给的高度
        var lineViews: ArrayList<View> = ArrayList()
        var lineWidthUsed = 0//记录已经使用了多宽的size
        var lineHeight = 0//一行的行高
        var parentNeededWidth: Int = 0;
        var parentNeededHeight: Int = 0
        for (index in 0 until count) {
            val child: View = getChildAt(index)
            if (child.visibility != View.GONE) {
                //将layoutParams转变成MeasureSpec
                val childWidthMeasureSpec: Int = getChildMeasureSpec(
                    widthMeasureSpec,
                    paddingLeft + paddingRight,
                    child.layoutParams.width
                )
                val childHeightMeasureSpec: Int = getChildMeasureSpec(
                    heightMeasureSpec,
                    paddingTop + paddingBottom,
                    child.layoutParams.height
                )
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
                //获取子View的宽高
                val childMeasuredWidth: Int = child.measuredWidth;
                val childMeasuredHeight: Int = child.measuredHeight
                if (childMeasuredWidth + lineWidthUsed + mHorizontalSpacing > selfWidth) {
                    allLine.add(lineViews)
                    lineHeights.add(lineHeight)

                    parentNeededWidth = max(parentNeededWidth,lineWidthUsed+mHorizontalSpacing)
                    parentNeededHeight += lineHeight + mVerticalSpacing

                    lineViews = ArrayList<View>()
                    lineWidthUsed = 0
                    lineHeight = 0
                }
                lineViews.add(child)
                lineWidthUsed += childMeasuredWidth + mHorizontalSpacing
                lineHeight = Math.max(lineHeight, childMeasuredHeight)
                //处理最后一行的数据
                if (index == count - 1) {
                    allLine.add(lineViews)
                    lineHeights.add(lineHeight)
                    parentNeededWidth = max(parentNeededWidth,lineWidthUsed + mHorizontalSpacing)
                    parentNeededHeight += lineHeight + mVerticalSpacing
                }
            }
        }
        val widthMode: Int = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode: Int = MeasureSpec.getMode(heightMeasureSpec)
        val realWidth: Int =
            if (widthMode == MeasureSpec.EXACTLY) selfWidth else parentNeededWidth
        val realHeight = if (heightMode == MeasureSpec.EXACTLY) selfHeight else parentNeededHeight
        setMeasuredDimension(realWidth,realHeight)
        Log.d(Companion.TAG, "onMeasure: allLine =  ${allLine}")
        Log.d(Companion.TAG, "onMeasure: lineHeight = ${lineHeight}")
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val lineCount:Int = allLine.size
        var curPaddingLeft:Int = paddingLeft
        var curPaddingTop :Int = paddingTop
        for (i in 0 until lineCount){
            var lineViews = allLine.get(i)
            var lineHeight = lineHeights.get(i)
            for (index in 0 until lineViews.size){
                var child = lineViews.get(index)
                val left = curPaddingLeft
                val top = curPaddingTop
                val right = curPaddingLeft + child.measuredWidth
                val bottom = curPaddingTop + child.measuredHeight
                child.layout(left,top,right,bottom)
                curPaddingLeft = right + mHorizontalSpacing
            }

            curPaddingLeft = paddingLeft
            curPaddingTop += lineHeight + mVerticalSpacing
        }
    }

    companion object {
        private const val TAG = "FlowLayout"
    }
}