package com.internaltest.sarahchatbotmvp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.internaltest.sarahchatbotmvp.adapters.ChatAdapter;
import com.internaltest.sarahchatbotmvp.models.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    RecyclerView chatView;
    ChatAdapter chatAdapter;
    List<Message> messageList = new ArrayList<>();
    EditText editMessage;
    ImageButton btnSend;
    FloatingActionButton info;
    ProgressDialog progressDialog;
    Button btnToggleDark;
    static int msgCounter = 0;

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chatView = findViewById(R.id.chatView);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        chatAdapter = new ChatAdapter(messageList, this);
        btnToggleDark = findViewById(R.id.btnToggleDark);
        chatView.setAdapter(chatAdapter);
        info= findViewById(R.id.info);
        info.setOnClickListener(v -> {
            AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
            ad.setIcon(R.mipmap.ic_launcher_round);
            ad.setTitle("Avisos:");
            ad.setMessage("APERTE O BOT??O VOLTAR NO SEU CELULAR PARA SAIR DESSA TELA\n" +
                    "Veja nossa Pol??tica de Privacidade: tinyurl.com/4saxzs79\n"+
                    "AVISO: ESSE APP AINDA EST?? EM FASE DE TESTES.\n " +
                    "Favor relatar quaisquer problemas para:\n teqbot.io59@gmail.com\n " +
                    "AVISO: Em raras ocasi??es, as frases ditas pelo app podem ter cunho ofensivo,violento," +
                    "racista, homof??bico, etc.\n Estamos trabalhando duro para criar um ambiente " +
                    "agr??davel e inclusivo para todos os nosso usu??rios.\n" +
                    "AVISO: Esse aplicativo n??o ?? substituto de um profissional de sa??de mental, e foi " +
                    "feito somente para prop??sitos de entretenimento.\n");
            ad.setPositiveButton("Sair", (dialog, which) -> dialog.dismiss());
            ad.show();
        });

        //implementando l??gica do bot??o modo escuro

        // salvando estado do app usando SharedPreferences
        SharedPreferences sharedPreferences
                = getSharedPreferences(
                "sharedPrefs", MODE_PRIVATE);
        final SharedPreferences.Editor editor
                = sharedPreferences.edit();
        final boolean isDarkModeOn
                = sharedPreferences
                .getBoolean(
                        "isDarkModeOn", true);

        // ativado quando o user reabre o app dps de aplicar o modo claro/escuro
        if (isDarkModeOn) {
            AppCompatDelegate
                    .setDefaultNightMode(
                            AppCompatDelegate
                                    .MODE_NIGHT_YES);
            btnToggleDark.setText(
                    "Desligar Modo Escuro");
        }
        else {
            AppCompatDelegate
                    .setDefaultNightMode(
                            AppCompatDelegate
                                    .MODE_NIGHT_NO);
            btnToggleDark
                    .setText(
                            "Ligar Modo Escuro");
        }

        btnToggleDark.setOnClickListener(
                view -> {
                    // Ativado qndo o user clica no btn de ligar/desligar o modo escuro
                    if (isDarkModeOn) {
                        AppCompatDelegate
                                .setDefaultNightMode(
                                        AppCompatDelegate
                                                .MODE_NIGHT_NO);
                        editor.putBoolean(
                                "isDarkModeOn", false);
                        editor.apply();

                        // Isso troca o texto do bot??o
                        btnToggleDark.setText(
                                "Ligar Modo Escuro");
                    }
                    else {
                        AppCompatDelegate
                                .setDefaultNightMode(
                                        AppCompatDelegate
                                                .MODE_NIGHT_YES);
                        editor.putBoolean(
                                "isDarkModeOn", true);
                        editor.apply();
                        btnToggleDark.setText(
                                "Desligar Modo Escuro");
                    }
                });

        btnSend.setOnClickListener(view -> {
            String message = editMessage.getText().toString();
            if (!message.isEmpty()) {
                messageList.add(new Message(message, false));
                editMessage.setText("");
                startMessageLoop(message);
                //mensagem de carregamento, que aparecer?? qndo o user mandar msg
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setTitle("Carregando mensagem");
                progressDialog.setMessage("Aguarde...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();
                progressDialog.setCancelable(false);
                Objects.requireNonNull(chatView.getAdapter()).notifyDataSetChanged();
                Objects.requireNonNull(chatView.getLayoutManager())
                        .scrollToPosition(messageList.size() - 1);
            } else {
                Toast.makeText(MainActivity.this, "Inserir Texto", Toast.LENGTH_SHORT).show();
            }
        });

        //msgs de aviso e para come??ar a conversa????o
        messageList.add(new Message("Primeira vez usando Samay? " +
                "Se sim, por favor leia os avisos apertando o s??mbolo no canto superior direito", true));
        chatAdapter.notifyDataSetChanged();
        Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);
        messageList.add(new Message("Ol??! Mande uma mensagem para mim! Eu demoro uns 10 segundinhos para responder", true));
        chatAdapter.notifyDataSetChanged();
        Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);
        messageList.add(new Message("Obrigado pela prefer??ncia! Divirta-se usando a Samay!", true));
        chatAdapter.notifyDataSetChanged();
        Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);

    }
    /*
    Como funciona o loop de mensagens:
    O usu??rio digita uma mensagem --> ela ?? traduzida para o ingl??s --> o blenderbot l?? a msg traduzida
    --> a resposta do blenderbot ?? traduzida de volta para o ptbr--> a msg traduzida ?? enfim mostrada
    para o user
    * */
    private void startMessageLoop(String message) {
        Log.i("start messsage",message);
        translateUserMsgToEnglish(message);
    }

    private void translateUserMsgToEnglish(String messagePTBR){
        Thread thread = new Thread(() -> {
            try{
                OkHttpClient client = new OkHttpClient();
                /*
                modelo de resposta
                String value = "{\"texts\": [\"hello. world!\"],\"tls\": [\"pt\"]}";
                \""+messagePTBR+"\"
                {"texts": ["ol??!"],"tls": ["en"],"sl":"pt"}
                 */

                MediaType mediaType = MediaType.parse("application/json");
                String value = "{\"texts\": [\""+messagePTBR+"\"],\"tls\": [\"en\"],\"sl\":\"pt\"}";
                RequestBody body = RequestBody.create(value, mediaType);
                Request request = new Request.Builder()
                        .url("https://google-translate54.p.rapidapi.com/translates")
                        .post(body)
                        .addHeader("content-type", "application/json")
                        .addHeader("X-RapidAPI-Host", "google-translate54.p.rapidapi.com")
                        .addHeader("X-RapidAPI-Key", "INSERT API KEY HERE")
                        .build();
                Response response = client.newCall(request).execute();
                String original = Objects.requireNonNull(response.body()).string();
                Log.i("original", original);
                //tirando caracteres lixo que n??o s??o parte da mensagem antes de mandar para a prox etapa
                //se eles ficarem na msg, pode causar erros
                String cleanFinalMessage = original.replace("\"code\":200,\"texts\":","")
                        .replace(",\"tl\":\"en\"","")
                        .replace(",\"sl\":\"pt\"","")
                        //tirando aspas, pois quebram as mensagems
                        .replaceAll("\"","")
                        .replace("[", "").replace("]", "")
                        .replace("{", "").replace("}", "");
                //nessa parte abaixo ele deixa somente letras, n??meros, sinais
                //de pontua????o passarem, tirando caracteres especiais como emojis
                String characterFilter = "[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]";
                String emojiless = cleanFinalMessage.replaceAll(characterFilter,"");
                Log.i("emojilessMessage", emojiless);
                blenderbotSendPost(emojiless);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void blenderbotSendPost(String message) {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL("https://hf.space/embed/Ideon/Samay/+/api/predict/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept","application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                List<String> list = new ArrayList<>();
                list.add(message);
                JSONArray array = new JSONArray();
                for(int i = 0; i < list.size(); i++) {
                    array.put(list.get(i));
                }
                JSONObject obj = new JSONObject();
                try {
                    obj.put("data", array);
                } catch(JSONException e) {
                    e.printStackTrace();
                }

                Log.i("JSON", obj.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(obj.toString());

                os.flush();
                os.close();

                Log.i("status response message", String.valueOf(conn.getResponseCode()));
                Log.i("Message response" , conn.getResponseMessage());
                //se o servidor respondeu como deveria, vai passar para a pr??xima etapa;
                // se n??o, ele vai parar aqui com um erro e o mostra para o usu??rio
                if (String.valueOf(conn.getResponseCode()).equals("200")){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    JsonObject botObj = JsonParser.parseReader(reader).getAsJsonObject();

                    String botMessage = botObj.get("data").getAsString();
                    //tirando caracteres lixo que n??o s??o parte da mensagem antes de mandar para a prox etapa
                    //se eles ficarem na msg, pode causar erros
                    String cleanFinalMessage = botMessage.replace("<s>","")
                            .replace("</s>","")
                            .replaceAll("\"","");

                    translateMachinePostToPortugueseAndSendMsgToUser(cleanFinalMessage);
                    Log.i("botMessage", cleanFinalMessage);

                    conn.disconnect();

                }else{
                    progressDialog.dismiss();
                    Toast.makeText(this, "algo deu errado", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void translateMachinePostToPortugueseAndSendMsgToUser(String messageEN){
        @SuppressLint("NotifyDataSetChanged") Thread thread = new Thread(() -> {
            try{
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/json");
                String value = "{\"texts\": [\""+messageEN+"\"],\"tls\": [\"pt\"],\"sl\":\"en\"}";
                RequestBody body = RequestBody.create(value, mediaType);
                Request request = new Request.Builder()
                        .url("https://google-translate54.p.rapidapi.com/translates")
                        .post(body)
                        .addHeader("content-type", "application/json")
                        .addHeader("X-RapidAPI-Host", "google-translate54.p.rapidapi.com")
                        .addHeader("X-RapidAPI-Key", "INSERT API KEY HERE")
                        .build();

                Response response = client.newCall(request).execute();
                Log.i("response", response.toString());
                String original = Objects.requireNonNull(response.body()).string();
                Log.i("original", original);

                //tirando caracteres lixo que n??o s??o parte da mensagem antes de mandar para o usu??rio
                String cleanFinalMessage = original.replace("[{\"code\":200,\"texts\":\"","")
                        .replace(",\"tl\":\"pt\"","")
                        .replace(",\"sl\":\"en\"","")
                        //tirando aspas, pois quebram as mensagems
                        .replaceAll("\"","")
                        .replace("[", "").replace("]", "")
                        .replace("{", "").replace("}", "")
                        //adicionando espa??amento depois dos sinais de pontua????o da resposta
                        .replace(".",". ")
                        .replace("!","! ")
                        .replace("?","? ")
                        .replace(",",", ")
                        //tirando aspas, pois quebram as mensagems
                        .replaceAll("\"","");
                //limpando mensagens em ingl??s que aparecem quando ela tem apostrofos
                if (original.contains("<i>")){
                    String startTag = "<i>";
                    String endTag = "</i>";
                    //removendo texto entre as tags
                    String textToRemove = cleanFinalMessage.substring(cleanFinalMessage.indexOf(startTag) + startTag.length(), cleanFinalMessage.indexOf(endTag));
                    String cleanFinalMessage2 = cleanFinalMessage.replaceAll(textToRemove, "");
                    String cleanFinalMessage3 = cleanFinalMessage2.replaceAll(startTag, "").replaceAll(endTag, "");
                    Log.i("finalMessagePTBR - c/ <i>", cleanFinalMessage3);
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        messageList.add(new Message(cleanFinalMessage3, true));
                        chatAdapter.notifyDataSetChanged();
                        Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);
                        msgCounter += 1;
                        if (msgCounter >= 10){
                            askRatings();
                        }
                    });
                }else{
                    Log.i("finalMessagePTBR s/ <i>", cleanFinalMessage);
                    if (!cleanFinalMessage.isEmpty()){
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            messageList.add(new Message(cleanFinalMessage, true));
                            chatAdapter.notifyDataSetChanged();
                            Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);
                            msgCounter += 1;
                            if (msgCounter >= 10){
                                askRatings();
                            }
                        });

                    }else {
                        Toast.makeText(this, "algo deu errado", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    // l??gica de pedir avalia????o na app store
    private void askRatings() {
        ReviewManager manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                ReviewInfo reviewInfo = task.getResult();
                Task<Void> flow = manager.launchReviewFlow(this, reviewInfo);
                flow.addOnCompleteListener(task2 -> {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                });
            } else {
                // There was some problem, continue regardless of the result.
                Toast.makeText(this, "algo deu errado", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
