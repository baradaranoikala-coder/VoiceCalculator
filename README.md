# ماشین حساب صوتی - آماده برای GitHub Actions

این پروژه به Android Studio نیاز ندارد. کافی است همه فایل‌های همین پوشه را در GitHub آپلود کنید. فایل `.github/workflows/build-apk.yml` به صورت خودکار APK تستی می‌سازد.

## روش سریع
1. یک Repository در GitHub بسازید.
2. تمام فایل‌ها و پوشه‌های داخل این پوشه را Upload کنید؛ نه خود ZIP و نه پوشه بالادستی.
3. بروید به تب Actions.
4. Workflow به نام Build Debug APK را اجرا کنید.
5. بعد از سبز شدن، از بخش Artifacts فایل APK را دانلود کنید.

## نکته
این پروژه عمداً `gradlew` ندارد. خود GitHub Actions نسخه Gradle را دانلود می‌کند و build می‌گیرد.
