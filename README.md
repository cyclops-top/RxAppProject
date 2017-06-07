## 说明

首先感谢下[RxPermissions](https://github.com/tbruyelle/RxPermissions)作者给我提供了用一个不显示```Fragment```做为依赖回调和生命周期相关的代理的思路。

Android很多时候会使用startActivityForResult，再重载```onActivityResult```方法进行回调，从而获取启动Activity返回的结果。但是这样的代码会将一个功能分开到2个代码段中来写，当代码达到一定量时，就变得很杂乱。同样的当进行PermissionRequest的时候也会遇到这个问题。

项目链接：[RxApp](https://github.com/JustinChengLu/RxAppProject)

主要功能：

- *startActivityForResult*返回*Observable<ActivityResult>*
- 请求权限返回*Observable<Permission>*
- 动态注册广播，返回实时广播的数据。在生命周期结束后自动取消注册
- 绑定生命周期。在**Observable**处理过程中，如果该界面（Activity或者Fragment）被销毁，那么自动取消订阅



基本原理：

​	根据当前界面（Activity或者Fragment），在其中添加一个不显示的```Fragment```，因为该```Fragment```有从属关系，所以其生命周期和依附的界面是相同的。基于此原理实现了ActivityForResult，Permission请求管理，动态Broadcast注册，RxJava相关异步调用绑定生命周期。

## StartActivityForResult

以Sample做基本详解。

需求启动```ResultActivity```并返回其Intent中传回的```TEXT```关键字的字串。

```java

RxApp.with(this)

.startActivityForObservable(intent, null)

.subscribe(activityResult -> {

if (activityResult.isOk()) {

String result =

activityResult.getData().getStringExtra("TEXT")

} else {

//用户取消操作

}

});
```

## RequsetPermission

### requestPermission

直接请求权限

需求：请求Camera和SMS权限

```java

RxApp.with(this)

.requestPermission(Manifest.permission.CAMERA, Manifest.permission.READ_SMS)

.subscribe(permissions ->{

for(Permission permission : permissions){

Log.d("RxApp","permission:"+permission)

}

});

```

### ensure

所有请求权限是否都允许

需求：Camera和SMS权限允许

```java

RxApp.with(this)

.ensure(Manifest.permission.CAMERA, Manifest.permission.READ_SMS)

.subscribe(isAllow -> {

Log.d("RxApp","isAllow:"+isAllow);

//todo 请求权限后的业务逻辑

});

```

## Broadcast

动态注册广播

需求：

- 注册自定义广播```"RxApp.TEST"```

- 可以随时取消

- 需要发送注册之前最后一次数据

- 在该Activity或者Fragment结束后自动取消注册

```java

//注册,当this的生命周期结束时会自动取消

Disposable broadcastDisposable =

RxApp.with(this).broadcast(true,"RxApp.TEST").subscribe(intent->{

//todo 业务逻辑

})

//动态取消

broadcastDisposable.dispose();

```

## bindLife

绑定生命周期

需求：隔1S发送一次广播```"RxApp.TEST"```，当依附的生命周期结束后自动停止

```java

Observable.interval(1, TimeUnit.SECONDS)

//绑定生命周期

.compose(RxApp.with(this).bindLife())

//设置订阅者在主线程响应

.observeOn(AndroidSchedulers.mainThread())

.subscribe(time->{

Intent intent = new Intent("RxApp.Test");

intent.putExtra("Time",(long)time);

sendBroadcast(intent);

});
```