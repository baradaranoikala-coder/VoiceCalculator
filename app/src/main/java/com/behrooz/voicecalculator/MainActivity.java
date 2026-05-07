package com.behrooz.voicecalculator;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.text.DecimalFormat;
import java.util.*;

public class MainActivity extends Activity {
    private static final int REQ_SPEECH = 10;
    private static final int REQ_AUDIO = 11;
    private EditText input;
    private TextView result;
    private LinearLayout historyBox;
    private final DecimalFormat fmt = new DecimalFormat("#,###.########");

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        buildUi();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 36, 32, 36);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("ماشین حساب صوتی");
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView hint = new TextView(this);
        hint.setText("مثال: دو به اضافه سه، بیست ضربدر چهار، ریشه دوم شانزده، دو به توان سه");
        hint.setTextSize(15);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, 20, 0, 20);
        root.addView(hint);

        input = new EditText(this);
        input.setHint("فرمان را بگویید یا تایپ کنید...");
        input.setTextSize(20);
        input.setSingleLine(false);
        input.setMinLines(2);
        input.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        root.addView(input, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER);
        buttons.setPadding(0, 24, 0, 24);
        root.addView(buttons);

        Button mic = new Button(this);
        mic.setText("🎤 بگو");
        buttons.addView(mic, new LinearLayout.LayoutParams(0, -2, 1));

        Button calc = new Button(this);
        calc.setText("محاسبه");
        buttons.addView(calc, new LinearLayout.LayoutParams(0, -2, 1));

        Button clear = new Button(this);
        clear.setText("پاک کردن");
        buttons.addView(clear, new LinearLayout.LayoutParams(0, -2, 1));

        result = new TextView(this);
        result.setText("نتیجه: —");
        result.setTextSize(30);
        result.setTypeface(Typeface.DEFAULT_BOLD);
        result.setGravity(Gravity.CENTER);
        result.setPadding(0, 16, 0, 24);
        root.addView(result, new LinearLayout.LayoutParams(-1, -2));

        TextView histTitle = new TextView(this);
        histTitle.setText("تاریخچه");
        histTitle.setTextSize(20);
        histTitle.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(histTitle);

        historyBox = new LinearLayout(this);
        historyBox.setOrientation(LinearLayout.VERTICAL);
        root.addView(historyBox);

        mic.setOnClickListener(v -> startVoice());
        calc.setOnClickListener(v -> calculate(input.getText().toString()));
        clear.setOnClickListener(v -> { input.setText(""); result.setText("نتیجه: —"); });
        setContentView(scroll);
    }

    private void startVoice() {
        if (android.os.Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQ_AUDIO);
            return;
        }
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR");
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "فرمان ریاضی را بگویید");
        try { startActivityForResult(i, REQ_SPEECH); }
        catch (Exception e) { Toast.makeText(this, "تشخیص گفتار روی این گوشی فعال نیست. لطفاً تایپ کنید.", Toast.LENGTH_LONG).show(); }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_SPEECH && resultCode == RESULT_OK && data != null) {
            ArrayList<String> r = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (r != null && !r.isEmpty()) { input.setText(r.get(0)); calculate(r.get(0)); }
        }
    }

    private void calculate(String text) {
        try {
            double value = PersianMath.eval(text);
            String out = fmt.format(value);
            result.setText("نتیجه: " + out);
            addHistory(text + " = " + out);
        } catch (Exception e) {
            result.setText("نتیجه: نامشخص");
            Toast.makeText(this, "فرمان را متوجه نشدم. نمونه: دو به اضافه سه", Toast.LENGTH_LONG).show();
        }
    }

    private void addHistory(String s) {
        TextView row = new TextView(this);
        row.setText(s);
        row.setTextSize(17);
        row.setPadding(0, 10, 0, 10);
        historyBox.addView(row, 0);
    }

    static class PersianMath {
        static final Map<String, Integer> nums = new HashMap<>();
        static { String[] names = {"صفر","یک","دو","سه","چهار","پنج","شش","هفت","هشت","نه","ده","یازده","دوازده","سیزده","چهارده","پانزده","شانزده","هفده","هجده","نوزده"}; for(int i=0;i<names.length;i++) nums.put(names[i], i); nums.put("بیست",20); nums.put("سی",30); nums.put("چهل",40); nums.put("پنجاه",50); nums.put("شصت",60); nums.put("هفتاد",70); nums.put("هشتاد",80); nums.put("نود",90); nums.put("صد",100); nums.put("دویست",200); nums.put("سیصد",300); nums.put("چهارصد",400); nums.put("پانصد",500); nums.put("ششصد",600); nums.put("هفتصد",700); nums.put("هشتصد",800); nums.put("نهصد",900); }

        static double eval(String raw) throws Exception {
            String s = normalize(raw);
            if (s.contains("درصد")) {
                String[] p = s.split("درصد");
                double a = parseNumber(p[0]);
                double b = p.length > 1 && p[1].trim().length() > 0 ? parseNumber(p[1]) : 100;
                return a * b / 100.0;
            }
            if (s.contains("ریشه دوم")) return Math.sqrt(parseNumber(s.replace("ریشه دوم", "")));
            if (s.contains("جذر")) return Math.sqrt(parseNumber(s.replace("جذر", "")));
            return binary(s, "به توان", Math::pow,
                    binary(s, "توان", Math::pow,
                    binary(s, "تقسیم بر", (a,b)->a/b,
                    binary(s, "تقسیم", (a,b)->a/b,
                    binary(s, "ضربدر", (a,b)->a*b,
                    binary(s, "ضرب در", (a,b)->a*b,
                    binary(s, "ضرب", (a,b)->a*b,
                    binary(s, "به اضافه", (a,b)->a+b,
                    binary(s, "اضافه", (a,b)->a+b,
                    binary(s, "جمع", (a,b)->a+b,
                    binary(s, "منهای", (a,b)->a-b,
                    binary(s, "کم", (a,b)->a-b, null))))))))))));
        }
        interface Op { double apply(double a, double b); }
        static Double binary(String s, String op, Op f, Double fallback) throws Exception {
            if (!s.contains(op)) return fallback;
            String[] p = s.split(op, 2);
            return f.apply(parseNumber(p[0]), parseNumber(p[1]));
        }
        static double parseNumber(String phrase) throws Exception {
            phrase = phrase.trim().replace(" و ", " ");
            phrase = toEnglishDigits(phrase);
            try { return Double.parseDouble(phrase.replace(" ", "")); } catch(Exception ignored) {}
            double total = 0, current = 0;
            for (String w : phrase.split("\\s+")) {
                if (w.length()==0) continue;
                if (w.equals("هزار")) { total += Math.max(1, current) * 1000; current = 0; }
                else if (w.equals("میلیون")) { total += Math.max(1, current) * 1000000; current = 0; }
                else if (nums.containsKey(w)) current += nums.get(w);
                else throw new Exception("unknown number " + w);
            }
            return total + current;
        }
        static String normalize(String s) { return s.replace('ي','ی').replace('ك','ک').replace("بعلاوه","به اضافه").replace("جمع با","به اضافه").replace("×"," ضرب ").replace("÷"," تقسیم ").trim(); }
        static String toEnglishDigits(String s) { return s.replace('۰','0').replace('۱','1').replace('۲','2').replace('۳','3').replace('۴','4').replace('۵','5').replace('۶','6').replace('۷','7').replace('۸','8').replace('۹','9').replace('٫','.'); }
    }
}
