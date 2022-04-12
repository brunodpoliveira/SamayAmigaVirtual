package com.internaltest.sarahchatbotmvp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chatView = findViewById(R.id.chatView);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        chatAdapter = new ChatAdapter(messageList, this);
        chatView.setAdapter(chatAdapter);
        info= findViewById(R.id.info);
        info.setOnClickListener(v -> {
            AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
            ad.setIcon(R.mipmap.ic_launcher_round);
            ad.setTitle("Avisos:");
            ad.setMessage("APERTE O BOTÃO VOLTAR NO SEU CELULAR PARA SAIR DESSA TELA\n" +
                    "Aviso de Privacidade\n: Esse app coleta as respostas digitadas pelos " +
                    "usuários durante suas conversas para melhorar a precisão das respostas " +
                    "do aplicativo, mesmo quando o app está fechado ou não está ativamente em uso " +
                    "por você.\n Ao fechar esse aviso, você indica que consente com esses termos.\n" +
                    "Caso não concorde, feche o aplicativo agora.\n" +
                    "AVISO: ESSE APP AINDA ESTÁ EM FASE DE TESTES.\n " +
                    "Favor relatar quaisquer problemas para:\n teqbot.io59@gmail.com\n " +
                    "AVISO: Em raras ocasiões, as frases ditas pelo app podem ter cunho ofensivo,violento," +
                    "racista, homofóbico, etc.\n Estamos trabalhando duro para criar um ambiente " +
                    "agrádavel e inclusivo para todos os nosso usuários.\n" +
                    "AVISO: Esse aplicativo não é substituto de um profissional de saúde mental, e foi " +
                    "feito somente para propósitos de entretenimento.\n");
            ad.setPositiveButton("Sair", (dialog, which) -> dialog.dismiss());
            ad.show();
        });

        btnSend.setOnClickListener(view -> {
            String message = editMessage.getText().toString();
            if (!message.isEmpty()) {
                messageList.add(new Message(message, false));
                editMessage.setText("");
                startMessageLoop(message);
                //mensagem de carregamento
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

        //msgs de aviso e para começar a conversação
        messageList.add(new Message("Primeira vez usando Samay? " +
                "Se sim, por favor leia os avisos apertando o símbolo no canto superior direito", true));
        chatAdapter.notifyDataSetChanged();
        Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);
        messageList.add(new Message("Olá! Mande uma mensagem para mim! Eu demoro uns segundinhos para responder", true));
        chatAdapter.notifyDataSetChanged();
        Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);
        messageList.add(new Message("Obrigado pela paciência! Divirta-se usando a Samay!", true));
        chatAdapter.notifyDataSetChanged();
        Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);

    }
    /*
    Como funciona o loop de mensagens:
    O usuário digita uma mensagem --> ela é traduzida para o inglês --> o blenderbor lê a msg traduzida
    --> a resposta do blenderbot é traduzida de volta para o ptbr--> a msg traduzida é enfim mostrada
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
                 */

                MediaType mediaType = MediaType.parse("application/json");
                String value = "{\"texts\": [\""+messagePTBR+"\"],\"tls\": [\"en\"]}";
                RequestBody body = RequestBody.create(mediaType, value);
                Request request = new Request.Builder()
                        .url("https://google-translate54.p.rapidapi.com/translates")
                        .post(body)
                        .addHeader("content-type", "application/json")
                        .addHeader("X-RapidAPI-Host", "google-translate54.p.rapidapi.com")
                        .addHeader("X-RapidAPI-Key", "INSERT_API_KEY")
                        .build();

                Response response = client.newCall(request).execute();
                String original = Objects.requireNonNull(response.body()).string();
                Log.i("original", original);
                //tirando caracteres lixo que não são parte da mensagem antes de mandar para a prox etapa
                String cleanFinalMessage = original.replace("\"code\":200,\"texts\":","");
                String cleanFinalMessage2 = cleanFinalMessage.replace(",\"tl\":\"en\"","");
                Log.i("cleanFinalMessage2", cleanFinalMessage2);
                blenderbotSendPost(cleanFinalMessage2);
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

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                JsonObject botObj = JsonParser.parseReader(reader).getAsJsonObject();

                String botMessage = botObj.get("data").getAsString();
                //tirando caracteres lixo que não são parte da mensagem antes de mandar para a prox etapa
                String cleanFinalMessage = botMessage.replace("<s>","");
                String cleanFinalMessage2 = cleanFinalMessage.replace("</s>","");

                translateMachinePostToPortugueseAndSendMsgToUser(cleanFinalMessage2);
                Log.i("botMessage", cleanFinalMessage2);

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void translateMachinePostToPortugueseAndSendMsgToUser(String messageEN){
        Thread thread = new Thread(() -> {
            try{
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/json");
                String value = "{\"texts\": [\""+messageEN+"\"],\"tls\": [\"pt\"]}";
                RequestBody body = RequestBody.create(mediaType, value);
                Request request = new Request.Builder()
                        .url("https://google-translate54.p.rapidapi.com/translates")
                        .post(body)
                        .addHeader("content-type", "application/json")
                        .addHeader("X-RapidAPI-Host", "google-translate54.p.rapidapi.com")
                        .addHeader("X-RapidAPI-Key", "INSERT_API_KEY")
                        .build();

                Response response = client.newCall(request).execute();
                Log.i("response", response.toString());
                String original = Objects.requireNonNull(response.body()).string();
                Log.i("original", original);

                //tirando caracteres lixo que não são parte da mensagem antes de mandar para o usuário
                String cleanFinalMessage = original.replace("[{\"code\":200,\"texts\":\"","")
                        .replace("\",\"tl\":\"pt\"}]","")
                        //adicionando espaçamento depois dos sinais de pontuação da resposta
                        .replace(".",". ")
                        .replace("!","! ")
                        .replace("?","? ")
                        .replace(",",", ")
                        //tirando aspas, pois quebram as mensagems
                        .replace("\"\"",", ");
                //limpando mensagens em inglês que aparecem quando ela tem apostrofos
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
                    });
                }else{
                    Log.i("finalMessagePTBR s/ <i>", cleanFinalMessage);
                    if (!cleanFinalMessage.isEmpty()){
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            messageList.add(new Message(cleanFinalMessage, true));
                            chatAdapter.notifyDataSetChanged();
                            Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);
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
}
