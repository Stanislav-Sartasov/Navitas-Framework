# Instructions for getting power_profile.xml

---

1. Download Linux wrapper script, saving [this link](https://raw.githubusercontent.com/iBotPeaches/Apktool/master/scripts/linux/apktool) as ***apktool***.
2. Download newest version of apktool [here](https://bitbucket.org/iBotPeaches/apktool/downloads) and rename downloaded ****.jar*** to ***apktool.jar***.
3. Move both files ***apktool.jar*** and ***apktool*** to ***/usr/local/bin*** (root needed, use ***sudo***), make sure both files are executable, using ***sudo chmod +x***.
4. Try running apktool via CLI.
   If you are missing Java use ***sudo apt install default-jre*** and ***sudo apt install openjdk-11-jre-headless*** to fix it.
5. Use ADB to get the the framework-res.apk: ***adb pull /system/framework/framework-res.apk*** and move it to ***/usr/local/bin***.
6. From ***/usr/local/bin***, use ***java -jar apktool.jar if framework-res.apk*** and ***java -jar apktool.jar d framework-res.apk***.
7. Path to the extracted file is: ***framework-res/res/xml/power_profile.xml***.
   Congratulations, you get a readable version of ***power_profile.xml***!

---

