# Pull-to-Refresh-Football-Animation

###### Pull-to-Refresh Footbal is a inhouse project of [Thinkwik](https://www.thinkwik.com/) and is a free and open source library for anyone to use.

## **DEMO**

<img src="https://github.com/dbthinkwik/Pull-to-Refresh-Football-Animation/blob/master/PullToRefresh.gif" data-canonical-src="https://github.com/dbthinkwik/Pull-to-Refresh-Football-Animation/blob/master/PullToRefresh.gif" width="300" height="600" />

## **USAGE**

[![](https://jitpack.io/v/dbthinkwik/Pull-to-Refresh-Football-Animation.svg)](https://jitpack.io/#dbthinkwik/Pull-to-Refresh-Football-Animation)

1. Add below code in your `build.gradle` file (located in root project folder structure).

```
allprojects {
   repositories {
      ...
      maven { url 'https://jitpack.io' }
   }
}
```

2. Add Dependency
```
dependencies {
   implementation 'com.github.thinkwik:Pull-to-Refresh-Football-Animation:-SNAPSHOT'
}
```

3. Include the `PullToRefreshView` widget in your layout.

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

4. Define your Refresh Delay
```
private val refreshDelay = 3000  (value is in milliseconds)
```

5. In your `onCreate` method refer to the View and setup `OnRefreshListener`.
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
