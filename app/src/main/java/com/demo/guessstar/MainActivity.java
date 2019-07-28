package com.demo.guessstar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.tv.TvContract;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {

    private Button button0;
    private Button button1;
    private Button button2;
    private Button button3;
    private ImageView imageViewStar;
    private String url = "http://www.posh24.se/kandisar/";
    //  private String url = "https://mail.ru/";
    private ArrayList<String> urls;
    private ArrayList<String> names;

    private int numberOfQuestion;
    private int numberOfRightAnswer;

    private ArrayList<Button> buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);

        imageViewStar = findViewById(R.id.imageViewStar);

        names = new ArrayList<>();
        urls = new ArrayList<>();

        buttons = new ArrayList<>();
        buttons.add(button0);
        buttons.add(button1);
        buttons.add(button2);
        buttons.add(button3);

        getContent();
        PlayGame();
    }

    private void getContent() {

        DownloadContentTask contentTask = new DownloadContentTask();
        try {

            String content = contentTask.execute(url).get();
            String start = "<p class=\"link\">Topp 100 kändisar</p>";
            String finish = "<div class=\"col-xs-12 col-sm-6 col-md-4\">";

            Pattern pattern = Pattern.compile(start + "(.*?)" + finish);
            Matcher matcher = pattern.matcher(content);

            String splitContent = "";
            while (matcher.find()) {
                splitContent = matcher.group(1);
            }
            //  Log.i("test", splitContent);

            Pattern patternImg = Pattern.compile("<img src=\"(.*?)\"");
            Pattern patternName = Pattern.compile("alt=\"(.*?)\"/>");
            Matcher matcherImg = patternImg.matcher(splitContent);
            Matcher matcherName = patternName.matcher(splitContent);

            while (matcherImg.find()) {
                urls.add(matcherImg.group(1));
            }

            while (matcherName.find()) {
                names.add(matcherName.group(1));
            }

        } catch (ExecutionException e) {
            //  Log.i("test", "e1");
            e.printStackTrace();
        } catch (InterruptedException e) {
            //  Log.i("test", "e2");
            e.printStackTrace();
        }

    }

    private void PlayGame() {

        generateQuestion();

        DownloadImageTask imageTask = new DownloadImageTask();
        try {

            Bitmap bitmap = imageTask.execute(urls.get(numberOfQuestion)).get();
            if (bitmap != null) {
                imageViewStar.setImageBitmap(bitmap);

                for (int i = 0; i < buttons.size(); i++) {
                    if (i == numberOfRightAnswer) {
                        buttons.get(i).setText(names.get(numberOfQuestion));
                    } else {
                        buttons.get(i).setText(names.get(generateWrongAnswer()));
                    }
                }
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void generateQuestion() {
        numberOfQuestion = (int) (Math.random() * names.size());
        numberOfRightAnswer = (int) (Math.random() * buttons.size());
    }

    private int generateWrongAnswer() {
        return (int) (Math.random() * names.size());
    }

    public void onClickAnswer(View view) {

        Button button = (Button) view;
        String tag = button.getTag().toString();
        if (Integer.parseInt(tag) == numberOfRightAnswer){
            Toast.makeText(this, "Верно!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Неверно, правильный ответ: " + names.get(numberOfQuestion), Toast.LENGTH_SHORT).show();
        }

        PlayGame();
    }

    private static class DownloadContentTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {

            Log.i("test", strings[0]);

            URL url = null;
            HttpURLConnection httpURLConnection = null;
            StringBuilder stringBuilder = new StringBuilder();
            try {
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String line = bufferedReader.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    line = bufferedReader.readLine();
                }

                return stringBuilder.toString();

            } catch (MalformedURLException e) {
                Log.i("test", "MalformedURLException");
                e.printStackTrace();
            } catch (IOException e) {
                Log.i("test", e.toString());
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            return null;
        }
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection httpURLConnection = null;

            StringBuilder stringBuilder = new StringBuilder();
            try {
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = httpURLConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            return null;
        }
    }

}
