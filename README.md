# morphing-button
Morphing Button Library with animation for tickets

![](morphing-button.gif)

How to use:
Step 1. Add the JitPack repository to your build file.
Add it in your root build.gradle at the end of repositories:
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
  
Step 2. Add the dependency
```
dependencies {
	        implementation 'com.github.yeltayev22:morphing-button:1.0.1'
	}  
```

Step 3. Use MorphingButton in your layout

```
<yeltayev.kz.moprhingbutton.MorphingButton
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginLeft="16dp"
            android:layout_marginTop="8dp"
            android:layout_marginRight="16dp"
            android:layout_marginBottom="8dp"
            app:text="@string/label_adult"
            app:button_height="80dp"
            app:circular_button_height="60dp"
            app:color="@color/colorPrimary"/>
```
