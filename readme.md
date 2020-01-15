# 日记本demo
## Mobile Application and Development (Android) 移动智能应用开发课程作业
### 简介
* 该日记本是基于 Android Studio 开发，仿 One UI 设计风格的日记本 APP。 
* 该日记本的设计方面与 One UI 基本相同，采用扁平化风格。主界面采用了
CoordinatorLayout + CollapsingToolbarLayout 来展示可展开的 Toolbar，使
用 CardView + RecyclerView 布局来展示日记。 
* 本项目使用 SQLite 技术，参考了子彦的项目，利用延迟/周期执行线程池来
定时保存日记的编辑记录。利用 ItemTouchHelper 实现了左滑删除、撤销删除，
拖动排序的功能。 
* 注册 Boardcast、利用自定义 Service 实现了背景播放音乐的功能，可暂停、
继续播放。 
 
<br/>
<br/>

### 展示
![pic1](https://github.com/Ryzin/NoteBook/raw/master/img/1.jpg)
![pic2](https://github.com/Ryzin/NoteBook/raw/master/img/2.jpg)