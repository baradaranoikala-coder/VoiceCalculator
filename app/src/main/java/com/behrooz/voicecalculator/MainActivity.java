package com.behrooz.voicecalculator;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    TextView expressionView, resultView, heardView, noteView;
    String expr = "";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 10);
        buildUi();
    }

    void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 28, 24, 24);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        TextView title = new TextView(this); title.setText("ماشین حساب صوتی"); title.setTextSize(24); title.setGravity(Gravity.CENTER); root.addView(title, new LinearLayout.LayoutParams(-1,-2));
        noteView = new TextView(this); noteView.setText("فرمان صوتی فعلاً آنلاین است. بعد از پایان صحبت، محاسبه خودکار انجام می‌شود."); noteView.setTextSize(13); noteView.setGravity(Gravity.CENTER); root.addView(noteView);
        heardView = new TextView(this); heardView.setText("فرمان شنیده‌شده: —"); heardView.setTextSize(15); heardView.setGravity(Gravity.RIGHT); root.addView(heardView, new LinearLayout.LayoutParams(-1,-2));
        expressionView = new TextView(this); expressionView.setText("0"); expressionView.setTextSize(30); expressionView.setGravity(Gravity.RIGHT); root.addView(expressionView, new LinearLayout.LayoutParams(-1,-2));
        resultView = new TextView(this); resultView.setText("نتیجه: —"); resultView.setTextSize(26); resultView.setGravity(Gravity.RIGHT); root.addView(resultView, new LinearLayout.LayoutParams(-1,-2));

        Button mic = new Button(this); mic.setText("🎙 فرمان صوتی"); mic.setTextSize(20); root.addView(mic, new LinearLayout.LayoutParams(-1,-2)); mic.setOnClickListener(v -> listen());

        String[][] keys = {{"C","⌫","%","/"},{"7","8","9","*"},{"4","5","6","-"},{"1","2","3","+"},{"0",".","=","="}};
        for (String[] row: keys) {
            LinearLayout line = new LinearLayout(this); line.setOrientation(LinearLayout.HORIZONTAL); root.addView(line, new LinearLayout.LayoutParams(-1,-2));
            for (String k: row) {
                Button btn = new Button(this); btn.setText(k); btn.setTextSize(22); line.addView(btn, new LinearLayout.LayoutParams(0,120,1)); btn.setOnClickListener(v -> press(((Button)v).getText().toString()));
            }
        }
        setContentView(root);
    }

    void listen() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) { toast("تشخیص صوتی روی این گوشی فعال نیست"); return; }
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR");
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "مثلاً بگویید: بیست و پنج ضربدر چهار");
        try { startActivityForResult(i, 100); } catch(Exception e){ toast("خطا در باز کردن فرمان صوتی"); }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> r = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (r != null && !r.isEmpty()) {
                String heard = r.get(0);
                heardView.setText("فرمان شنیده‌شده: " + heard);
                expr = wordsToExpression(heard);
                update();
                calculate();
            }
        }
    }

    void press(String k) {
        if (k.equals("C")) { expr=""; resultView.setText("نتیجه: —"); update(); return; }
        if (k.equals("⌫")) { if(expr.length()>0) expr=expr.substring(0,expr.length()-1); update(); return; }
        if (k.equals("=")) { calculate(); return; }
        if (k.equals("%")) { expr += "%"; update(); return; }
        expr += k; update();
    }
    void update(){ expressionView.setText(expr.isEmpty()?"0":expr); }
    void calculate(){ try { double v = eval(expr.replace("%","/100")); resultView.setText("نتیجه: " + fmt(v)); } catch(Exception e){ resultView.setText("نتیجه: قابل محاسبه نیست"); } }
    String fmt(double v){ if (Math.abs(v - Math.round(v)) < 1e-9) return String.valueOf(Math.round(v)); return String.valueOf(v); }
    void toast(String s){ Toast.makeText(this,s,Toast.LENGTH_LONG).show(); }

    String norm(String s){
        char[] fa="۰۱۲۳۴۵۶۷۸۹٠١٢٣٤٥٦٧٨٩".toCharArray(); String en="01234567890123456789";
        StringBuilder out=new StringBuilder();
        for(char c:s.toCharArray()){ int idx=new String(fa).indexOf(c); out.append(idx>=0?en.charAt(idx):c); }
        return out.toString().toLowerCase(Locale.ROOT).replace("،"," ").replace(","," ");
    }
    String wordsToExpression(String input){
        String s = norm(input);
        s = s.replace("به علاوه", " + ").replace("جمع", " + ").replace("بعلاوه", " + ").replace("اضافه", " + ");
        s = s.replace("منهای", " - ").replace("تفریق", " - ").replace("کم", " - ");
        s = s.replace("ضربدر", " * ").replace("ضرب در", " * ").replace("ضرب", " * ").replace("در", " * ").replace("ایکس", " * ");
        s = s.replace("تقسیم بر", " / ").replace("تقسیم", " / ").replace("بخش بر", " / ");
        s = s.replace("ممیز", ".");
        String[] parts = s.trim().split("\\s+");
        StringBuilder out = new StringBuilder(); ArrayList<String> buf = new ArrayList<>();
        for(String p: parts){
            if(p.matches("[+\\-*/]") || p.equals("%")) { appendNumber(out,buf); out.append(p); buf.clear(); }
            else if(p.matches("[0-9.]+")) { appendNumber(out,buf); out.append(p); }
            else buf.add(p);
        }
        appendNumber(out,buf);
        return out.toString();
    }
    void appendNumber(StringBuilder out, ArrayList<String> buf){ if(buf.isEmpty()) return; Long n=parseFaNumber(buf); if(n!=null) out.append(n); buf.clear(); }
    Long parseFaNumber(List<String> w){
        HashMap<String,Integer> m=new HashMap<>();
        String[] ones={"صفر","یک","دو","سه","چهار","پنج","شش","هفت","هشت","نه","ده","یازده","دوازده","سیزده","چهارده","پانزده","شانزده","هفده","هجده","نوزده"};
        for(int i=0;i<ones.length;i++)m.put(ones[i],i);
        m.put("بیست",20);m.put("سی",30);m.put("چهل",40);m.put("پنجاه",50);m.put("شصت",60);m.put("هفتاد",70);m.put("هشتاد",80);m.put("نود",90);
        m.put("صد",100);m.put("یکصد",100);m.put("دویست",200);m.put("سیصد",300);m.put("چهارصد",400);m.put("پانصد",500);m.put("ششصد",600);m.put("هفتصد",700);m.put("هشتصد",800);m.put("نهصد",900);
        long total=0, cur=0; boolean found=false;
        for(String x:w){ if(x.equals("و")) continue; if(x.equals("هزار")){ total += (cur==0?1:cur)*1000; cur=0; found=true; continue;} if(x.equals("میلیون")){ total += (cur==0?1:cur)*1000000; cur=0; found=true; continue;} Integer v=m.get(x); if(v==null) continue; cur+=v; found=true; }
        return found ? total+cur : null;
    }

    double eval(String s){
        ArrayList<String> toks=new ArrayList<>(); String num="";
        for(char c:s.toCharArray()){ if(Character.isDigit(c)||c=='.') num+=c; else if("+-*/".indexOf(c)>=0){ if(!num.isEmpty()){toks.add(num);num="";} toks.add(String.valueOf(c)); } }
        if(!num.isEmpty()) toks.add(num);
        for(int i=1;i<toks.size()-1;){ String op=toks.get(i); if(op.equals("*")||op.equals("/")){ double a=Double.parseDouble(toks.get(i-1)), b=Double.parseDouble(toks.get(i+1)); double r=op.equals("*")?a*b:a/b; toks.set(i-1,String.valueOf(r)); toks.remove(i); toks.remove(i); } else i+=2; }
        double res=Double.parseDouble(toks.get(0)); for(int i=1;i<toks.size()-1;i+=2){ res += toks.get(i).equals("+")?Double.parseDouble(toks.get(i+1)):-Double.parseDouble(toks.get(i+1)); }
        return res;
    }
}
