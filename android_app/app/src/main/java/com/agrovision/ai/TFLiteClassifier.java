package com.agrovision.ai;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TFLiteClassifier {

    public static class PredictionResult {
        public String label;
        public String displayName;
        public float confidence;
        public String treatment;

        public PredictionResult(String label, String displayName, float confidence, String treatment) {
            this.label = label;
            this.displayName = displayName;
            this.confidence = confidence;
            this.treatment = treatment;
        }
    }

    private Interpreter diseaseInterpreter;
    private Interpreter insectInterpreter;

    private List<String> diseaseClasses = new ArrayList<>();
    private Map<String, String> diseaseDisplayNames = new HashMap<>();
    private Map<String, String> diseaseRecommendations = new HashMap<>();

    private List<String> insectClasses = new ArrayList<>();
    private Map<String, String> insectDisplayNames = new HashMap<>();
    private Map<String, String> insectRecommendations = new HashMap<>();

    public TFLiteClassifier(Context context) throws Exception {
        diseaseInterpreter = new Interpreter(loadModelFile(context, "disease_model.tflite"));
        insectInterpreter = new Interpreter(loadModelFile(context, "insect_model.tflite"));

        loadJSONConfig(context, "disease_labels.json", diseaseClasses, diseaseDisplayNames, diseaseRecommendations);
        loadJSONConfig(context, "insect_labels.json", insectClasses, insectDisplayNames, insectRecommendations);
    }

    private MappedByteBuffer loadModelFile(Context context, String modelFilename) throws Exception {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void loadJSONConfig(Context context, String jsonFilename, List<String> classesList,
                                Map<String, String> displayMap, Map<String, String> recMap) throws Exception {
        InputStream is = context.getAssets().open(jsonFilename);
        byte[] buffer = new byte[is.available()];
        is.read(buffer);
        is.close();

        String jsonStr = new String(buffer, "UTF-8");
        JSONObject obj = new JSONObject(jsonStr);

        JSONArray classesArr = obj.getJSONArray("classes");
        for (int i = 0; i < classesArr.length(); i++) {
            classesList.add(classesArr.getString(i));
        }

        JSONObject displayObj = obj.getJSONObject("display_names");
        for (String key : classesList) {
            if (displayObj.has(key)) {
                displayMap.put(key, displayObj.getString(key));
            }
        }

        JSONObject recObj = obj.getJSONObject("recommendations");
        for (String key : displayMap.values()) {
            if (recObj.has(key)) {
                recMap.put(key, recObj.getString(key));
            }
        }
    }

    public PredictionResult predictDisease(Bitmap bitmap) {
        ByteBuffer inputBuffer = preprocessBitmap(bitmap);
        float[][] output = new float[1][diseaseClasses.size()];
        diseaseInterpreter.run(inputBuffer, output);

        int bestIndex = 0;
        float maxProb = output[0][0];
        for (int i = 1; i < output[0].length; i++) {
            if (output[0][i] > maxProb) {
                maxProb = output[0][i];
                bestIndex = i;
            }
        }

        String rawLabel = diseaseClasses.get(bestIndex);
        String displayName = diseaseDisplayNames.getOrDefault(rawLabel, rawLabel);
        String treatment = diseaseRecommendations.getOrDefault(displayName, "Consult an agricultural expert for specific treatment.");

        return new PredictionResult(rawLabel, displayName, maxProb, treatment);
    }

    public PredictionResult predictInsect(Bitmap bitmap) {
        ByteBuffer inputBuffer = preprocessBitmap(bitmap);
        float[][] output = new float[1][insectClasses.size()];
        insectInterpreter.run(inputBuffer, output);

        int bestIndex = 0;
        float maxProb = output[0][0];
        for (int i = 1; i < output[0].length; i++) {
            if (output[0][i] > maxProb) {
                maxProb = output[0][i];
                bestIndex = i;
            }
        }

        String rawLabel = insectClasses.get(bestIndex);
        String displayName = insectDisplayNames.getOrDefault(rawLabel, rawLabel);
        String treatment = insectRecommendations.getOrDefault(displayName, "Consult an agricultural expert for specific treatment.");

        return new PredictionResult(rawLabel, displayName, maxProb, treatment);
    }

    private ByteBuffer preprocessBitmap(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[224 * 224];
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.getWidth(), 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());

        for (int pixelValue : intValues) {
            float r = ((pixelValue >> 16) & 0xFF) / 255.0f;
            float g = ((pixelValue >> 8) & 0xFF) / 255.0f;
            float b = (pixelValue & 0xFF) / 255.0f;

            byteBuffer.putFloat(r);
            byteBuffer.putFloat(g);
            byteBuffer.putFloat(b);
        }
        return byteBuffer;
    }
}
