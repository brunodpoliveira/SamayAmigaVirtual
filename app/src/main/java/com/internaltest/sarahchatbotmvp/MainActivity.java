package com.internaltest.sarahchatbotmvp;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonElement;
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
import java.text.Normalizer;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    RecyclerView chatView;
    ChatAdapter chatAdapter;
    List<Message> messageList = new ArrayList<>();
    EditText editMessage;
    ImageButton btnSend;
    FloatingActionButton info;

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
                URL url = new URL("https://lecto-translation.p.rapidapi.com/v1/translate/json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("content-type", "application/json");
                conn.setRequestProperty("X-RapidAPI-Host", "lecto-translation.p.rapidapi.com");
                conn.setRequestProperty("X-RapidAPI-Key", "7956072f06msheafb2efb88795b3p10d73ajsn4d99f9a6472b");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                // "{\"to\":[\"en\"],\"from\":\"pt-br\",\"json\":\"{\\\"\\\":\\\"Olá\\\"}\"}"
                String body ="{\"to\":[\"en\"],\"from\":\"pt-br\",\"json\":\"{\\\"\\\":\\\""+messagePTBR+"\\\"}\"}";

                //removendo acentos da mensagem, pois a api parece não ler mensagens com acentos corretamente

                String nfdNormalizedString = Normalizer.normalize(body, Normalizer.Form.NFD);
                Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
                String normalizedString = pattern.matcher(nfdNormalizedString).replaceAll("");
                Log.i("normalizedString", normalizedString);
                os.writeBytes(normalizedString);
                os.flush();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                JsonObject finalMessageObj = JsonParser.parseReader(reader).getAsJsonObject();
                Log.i("finalMessageObjEN", finalMessageObj.toString());

                //I/finalMessageObjEN: {"translations":[{"to":"en","translated":["{\"0\":\"Hey.\"}"]}],"from":"pt-br","protected_keys":[],"translated_characters":9}
                JsonElement finalMessageEN = finalMessageObj.getAsJsonArray("translations");
                Log.i("finalMessageEN", finalMessageEN.toString());
                blenderbotSendPost(finalMessageEN.toString());
                conn.disconnect();
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
                URL url = new URL("https://hf.space/gradioiframe/Ideon/SamayMVP/+/api/predict/");
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
                translateMachinePostToPortugueseAndSendMsgToUser(botMessage);
                Log.i("botMessage", botMessage);

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
                URL url = new URL("https://lecto-translation.p.rapidapi.com/v1/translate/json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("content-type", "application/json");
                conn.setRequestProperty("X-RapidAPI-Host", "lecto-translation.p.rapidapi.com");
                conn.setRequestProperty("X-RapidAPI-Key", "7956072f06msheafb2efb88795b3p10d73ajsn4d99f9a6472b");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                String body = "{\"to\":[\"pt-br\"],\"from\":\"en\",\"json\":\"{\\\"\\\":\\\""+messageEN+"\\\"}\"}";
                os.writeBytes(body);
                os.flush();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                JsonObject finalMessageObj = JsonParser.parseReader(reader).getAsJsonObject();
                JsonElement finalMessagePTBR = finalMessageObj.getAsJsonArray("translations");
                String original = finalMessagePTBR.toString();
                //tirando caracteres lixo que não são parte da mensagem antes de mandar para o usuário

                String cleanFinalMessage = original.replace("[{\"to\":\"pt-br\",\"translated\":[\"{\\\"0\\\":\\\"<s>","");
                String cleanFinalMessage2 = cleanFinalMessage.replace("</s>\\\"}\"]}]","");
                Log.i("finalMessagePTBR", cleanFinalMessage2);
                if (!cleanFinalMessage2.isEmpty()){
                    runOnUiThread(() -> {
                        messageList.add(new Message(cleanFinalMessage2, true));
                        chatAdapter.notifyDataSetChanged();
                        Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);
                    });

                }else {
                    Toast.makeText(this, "algo deu errado", Toast.LENGTH_SHORT).show();
                }

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }
}
