package com.thinkwik.pulltorefresh

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.support.v4.content.ContextCompat
import android.support.v4.view.MotionEventCompat
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.*
import android.view.animation.*
import android.widget.AbsListView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.thinkwik.pulltorefresh.utils.Utils

class FootballPullToRefreshView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs) {

    private val mainLayout: LinearLayout
    private val fmLayout: FrameLayout
    private val fmParams: FrameLayout.LayoutParams
    private val ivParams: FrameLayout.LayoutParams
    private val llParams: ViewGroup.LayoutParams
    private val set: AnimationSet
    private val animationDrawable: AnimationDrawable
    private var mTarget: View? = null
    private val mRefreshView: ImageView
    private val mDecelerateInterpolator: Interpolator
    private val mTouchSlop: Int
    private var totalDragDistance: Int? = null
    private var mCurrentDragPercent = 0f
    private var mCurrentOffsetTop = 0
    private var mRefreshing = false
    private var mActivePointerId = 0
    private var mIsBeingDragged = false
    private var mInitialMotionY = 0f
    private var mFrom = 0
    private var mFromDragPercent = 0f
    private var mNotify = false
    private var mListener: OnRefreshListener? = null
    private var mTargetPaddingTop = 0
    private var mTargetPaddingBottom = 0
    private var mTargetPaddingRight = 0
    private var mTargetPaddingLeft = 0

    private val mAnimateToCorrectPosition = object : Animation() {
        public override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            val targetTop: Int
            val endTarget = totalDragDistance
            targetTop = mFrom + ((endTarget?.minus(mFrom))?.times(interpolatedTime))!!.toInt()
            val offset = targetTop - mTarget!!.top

            mCurrentDragPercent = mFromDragPercent - (mFromDragPercent - 1.0f) * interpolatedTime
            setTargetOffsetTop(offset, false /* requires update */)
        }
    }

    private val mAnimateToStartPosition = object : Animation() {
        public override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            moveToStart(interpolatedTime)
        }
    }
    private val mToStartListener = object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) {}

        override fun onAnimationRepeat(animation: Animation) {}

        override fun onAnimationEnd(animation: Animation) {
            //            animationDrawable.stop();
            mCurrentOffsetTop = mTarget!!.top
        }
    }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.RefreshView)
        a.recycle()
        mDecelerateInterpolator = DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR)
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        totalDragDistance = Utils.convertDpToPixel(context, DRAG_MAX_DISTANCE)

        mainLayout = LinearLayout(context)
        llParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.START.toFloat())
        mainLayout.gravity = Gravity.TOP or Gravity.CENTER
        mainLayout.layoutParams = llParams

        fmLayout = FrameLayout(context)
        fmParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        fmLayout.setBackgroundResource(R.drawable.ic_bg)
        fmLayout.layoutParams = fmParams


        mRefreshView = ImageView(context)
        ivParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mRefreshView.layoutParams = ivParams


        fmLayout.addView(mRefreshView)
        mainLayout.addView(fmLayout)

        mRefreshView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.loading_animation))
        animationDrawable = mRefreshView.drawable as AnimationDrawable
        setWillNotDraw(false)
        set = AnimationSet(false)
        set.duration = 200
        addView(mainLayout)
        ViewCompat.setChildrenDrawingOrderEnabled(this, true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        ensureTarget()
        if (mTarget == null)
            return

        widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(measuredWidth - paddingRight - paddingLeft, View.MeasureSpec.EXACTLY)
        heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(measuredHeight - paddingTop - paddingBottom, View.MeasureSpec.EXACTLY)
        mTarget!!.measure(widthMeasureSpec, heightMeasureSpec)
        mainLayout.measure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun ensureTarget() {
        if (mTarget != null)
            return
        if (childCount > 0) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child !== mRefreshView) {
                    mTarget = child
                    mTargetPaddingBottom = mTarget!!.paddingBottom
                    mTargetPaddingLeft = mTarget!!.paddingLeft
                    mTargetPaddingRight = mTarget!!.paddingRight
                    mTargetPaddingTop = mTarget!!.paddingTop
                }
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!isEnabled || canChildScrollUp() || mRefreshing) {
            return false
        }

        val action = MotionEventCompat.getActionMasked(ev)

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                setTargetOffsetTop(0, true)
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0)
                mIsBeingDragged = false
                val initialMotionY = getMotionEventY(ev, mActivePointerId)
                if (initialMotionY == -1f) {
                    return false
                }
                mInitialMotionY = initialMotionY
            }
            MotionEvent.ACTION_MOVE -> {
                if (mActivePointerId == INVALID_POINTER) {
                    return false
                }
                val y = getMotionEventY(ev, mActivePointerId)
                if (y == -1f) {
                    return false
                }
                val yDiff = y - mInitialMotionY
                if (yDiff > mTouchSlop && !mIsBeingDragged) {
                    mIsBeingDragged = true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mIsBeingDragged = false
                mActivePointerId = INVALID_POINTER
            }
            MotionEventCompat.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)
        }
        return mIsBeingDragged
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {

        if (!mIsBeingDragged) {
            return super.onTouchEvent(ev)
        }

        val action = MotionEventCompat.getActionMasked(ev)

        when (action) {
            MotionEvent.ACTION_MOVE -> {

                val pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId)
                if (pointerIndex < 0) {
                    return false
                }

                val y = MotionEventCompat.getY(ev, pointerIndex)
                val yDiff = y - mInitialMotionY
                val scrollTop = yDiff * DRAG_RATE
                mCurrentDragPercent = scrollTop / totalDragDistance!!
                if (mCurrentDragPercent < 0) {
                    return false
                }
                val boundedDragPercent = Math.min(1f, Math.abs(mCurrentDragPercent))
                val extraOS = Math.abs(scrollTop) - totalDragDistance!!
                val slingshotDist = totalDragDistance!!.toFloat()
                val tensionSlingshotPercent = Math.max(0f,
                        Math.min(extraOS, slingshotDist * 2) / slingshotDist)
                val tensionPercent = (tensionSlingshotPercent / 4 - Math.pow(
                        (tensionSlingshotPercent / 4).toDouble(), 2.0)).toFloat() * 2f
                val extraMove = slingshotDist * tensionPercent / 2
                val targetY = (slingshotDist * boundedDragPercent + extraMove).toInt()

                setTargetOffsetTop(targetY - mCurrentOffsetTop, true)
            }
            MotionEventCompat.ACTION_POINTER_DOWN -> {
                val index = MotionEventCompat.getActionIndex(ev)
                mActivePointerId = MotionEventCompat.getPointerId(ev, index)
            }
            MotionEventCompat.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (mActivePointerId == INVALID_POINTER) {
                    return false
                }
                val pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId)
                val y = MotionEventCompat.getY(ev, pointerIndex)
                val overScrollTop = (y - mInitialMotionY) * DRAG_RATE
                mIsBeingDragged = false
                if (overScrollTop > totalDragDistance!!) {
                    setRefreshing(true, true)
                } else {
                    mRefreshing = false
                    animateOffsetToStartPosition()
                }
                mActivePointerId = INVALID_POINTER
                return false
            }
        }
        return true
    }

    private fun animateOffsetToStartPosition() {
        mFrom = mCurrentOffsetTop
        mFromDragPercent = mCurrentDragPercent
        val animationDuration = Math.abs((MAX_OFFSET_ANIMATION_DURATION * mFromDragPercent).toLong())

        mAnimateToStartPosition.reset()
        mAnimateToStartPosition.duration = animationDuration
        mAnimateToStartPosition.interpolator = mDecelerateInterpolator
        mAnimateToStartPosition.setAnimationListener(mToStartListener)
        mRefreshView.clearAnimation()
        mRefreshView.startAnimation(mAnimateToStartPosition)
    }

    private fun animateOffsetToCorrectPosition() {
        mFrom = mCurrentOffsetTop
        mFromDragPercent = mCurrentDragPercent

        mAnimateToCorrectPosition.reset()
        mAnimateToCorrectPosition.duration = MAX_OFFSET_ANIMATION_DURATION.toLong()
        mAnimateToCorrectPosition.interpolator = mDecelerateInterpolator
        mRefreshView.clearAnimation()
        mRefreshView.startAnimation(mAnimateToCorrectPosition)

        if (mRefreshing) {
            animationDrawable.start()
//            mFootball.startAnimation(set)
//            imgBg.startAnimation(bgAnimation)
            if (mNotify) {
                if (mListener != null) {
                    mListener!!.onRefresh()
                }
            }
        } else {
            //            animationDrawable.stop();
        }
        mCurrentOffsetTop = mTarget!!.top
        mTarget!!.setPadding(mTargetPaddingLeft, mTargetPaddingTop, mTargetPaddingRight, totalDragDistance!!)
    }

    private fun moveToStart(interpolatedTime: Float) {
        val targetTop = mFrom - (mFrom * interpolatedTime).toInt()
        val targetPercent = mFromDragPercent * (1.0f - interpolatedTime)
        val offset = targetTop - mTarget!!.top

        mCurrentDragPercent = targetPercent
        mTarget!!.setPadding(mTargetPaddingLeft, mTargetPaddingTop, mTargetPaddingRight, mTargetPaddingBottom + targetTop)
        setTargetOffsetTop(offset, false)
    }

    fun setRefreshing(refreshing: Boolean) {
        if (mRefreshing != refreshing) {
            setRefreshing(refreshing, false /* notify */)
        }
    }

    private fun setRefreshing(refreshing: Boolean, notify: Boolean) {
        if (mRefreshing != refreshing) {
            mNotify = notify
            ensureTarget()
            mRefreshing = refreshing
            if (mRefreshing) {
                animateOffsetToCorrectPosition()
            } else {
                animateOffsetToStartPosition()
            }
        }
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = MotionEventCompat.getActionIndex(ev)
        val pointerId = MotionEventCompat.getPointerId(ev, pointerIndex)
        if (pointerId == mActivePointerId) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex)
        }
    }

    private fun getMotionEventY(ev: MotionEvent, activePointerId: Int): Float {
        val index = MotionEventCompat.findPointerIndex(ev, activePointerId)
        return if (index < 0) {
            -1f
        } else MotionEventCompat.getY(ev, index)
    }

    private fun setTargetOffsetTop(offset: Int, requiresUpdate: Boolean) {
        mTarget!!.offsetTopAndBottom(offset)
        mCurrentOffsetTop = mTarget!!.top
        if (requiresUpdate) {
            invalidate()
        }
    }

    private fun canChildScrollUp(): Boolean {
        return if (mTarget is AbsListView) {
            val absListView = mTarget as AbsListView?
            absListView!!.childCount > 0 && (absListView.firstVisiblePosition > 0 || absListView.getChildAt(0)
                    .top < absListView.paddingTop)
        } else {
            mTarget!!.scrollY > 0
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

        ensureTarget()
        if (mTarget == null)
            return

        val height = measuredHeight
        val width = measuredWidth
        val left = paddingLeft
        val top = paddingTop
        val right = paddingRight
        val bottom = paddingBottom

        mTarget!!.layout(left, top + mCurrentOffsetTop, left + width - right, top + height - bottom + mCurrentOffsetTop)
        mainLayout.layout(left, top, left + width - right, top + height - bottom)
    }

    fun setOnRefreshListener(listener: OnRefreshListener) {
        mListener = listener
    }

    interface OnRefreshListener {
        fun onRefresh()
    }

    companion object {
        private const val MAX_OFFSET_ANIMATION_DURATION = 1000
        private const val DRAG_MAX_DISTANCE = 120
        private const val DRAG_RATE = .5f
        private const val DECELERATE_INTERPOLATION_FACTOR = 1f
        private const val INVALID_POINTER = -1
    }

}
