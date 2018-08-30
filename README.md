# Pull-to-Refresh-Football-Animation

###### Pull-to-Refresh Footbal is a inhouse project of [Thinkwik](https://www.thinkwik.com/) and is a free and open source library for anyone to use.


## **USAGE**

1. Include the `PullToRefreshView` widget in your layout.

```
<com.thinkwik.pulltorefresh.FootballPullToRefreshView
   android:id="@+id/mPullToRefreshView"
   android:layout_width="match_parent"
   android:layout_height="match_parent">

   <android.support.v7.widget.RecyclerView
       android:id="@+id/recyclerView"
       android:layout_width="match_parent"
       android:layout_height="wrap_content"
       android:background="@color/white" />
</com.thinkwik.pulltorefresh.FootballPullToRefreshView>
```


2. Define your Refresh Delay
```
private val refreshDelay = 3000  (value is in milliseconds)
```

3. In your `onCreate` method refer to the View and setup `OnRefreshListener`.
```
mPullToRefreshView.setOnRefreshListener(object : FootballPullToRefreshView.OnRefreshListener {
   override fun onRefresh() {
       mPullToRefreshView.postDelayed({ mPullToRefreshView.setRefreshing(false) }, refreshDelay.toLong())
   }
})
```

## **Compatibility**

1. Kotlin
2. API level >=17
