package com.example.plantcare.Activity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Classifier {
    // TensorFlow Lite Interpreter for running the model
    private Interpreter INTERPRETER;

    // List to store the labels corresponding to the classes in the model
    private List<String> LABEL_LIST;

    // Model input image size and pixel size (for RGB images)
    private final int INPUT_SIZE;
    private final int PIXEL_SIZE = 3; // RGB images have 3 channels
    private final int IMAGE_MEAN = 0; // Image mean for normalization
    private final float IMAGE_STD = 255.0f; // Image standard deviation for normalization

    // Maximum number of results to return and threshold for filtering results
    private final int MAX_RESULTS = 3;
    private final float THRESHOLD = 0.4f; // Confidence threshold for displaying results

    // Class representing the recognition result
    public static class Recognition {
        private String id = "";        // ID of the class
        private String title = "";     // Title (label) of the recognized class
        private float confidence = 0F; // Confidence of the prediction

        public Recognition(String id, String title, float confidence) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
        }

        // Return a string representation of the recognition result
        @Override
        public String toString() {
            return "Title = " + title + ", Confidence = " + confidence;
        }

        // Getter methods for ID, title, and confidence
        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public float getConfidence() {
            return confidence;
        }
    }

    // Constructor to initialize the classifier with model and label paths, and input size
    public Classifier(AssetManager assetManager, String modelPath, String labelPath, int inputSize) throws IOException {
        // Initialize the TensorFlow Lite Interpreter with the model file
        INTERPRETER = new Interpreter(loadModelFile(assetManager, modelPath));

        // Load the list of labels from the label file
        LABEL_LIST = loadLabelList(assetManager, labelPath);

        // Set the input size (e.g., 224x224)
        INPUT_SIZE = inputSize;
    }

    // Load the TFLite model from the assets folder into memory
    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        // Open the model file and map it to memory for faster access
        FileInputStream inputStream = new FileInputStream(assetManager.openFd(modelPath).getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = assetManager.openFd(modelPath).getStartOffset();
        long declaredLength = assetManager.openFd(modelPath).getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Load the label file (list of class labels) from assets
    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException {
        // Read the labels line by line and store them in a list
        List<String> labels = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;
        while ((line = reader.readLine()) != null) {
            labels.add(line);
        }
        reader.close();
        return labels;
    }

    // Perform image recognition on the provided bitmap and return a list of recognitions
    public List<Recognition> recognizeImage(Bitmap bitmap) {
        // Resize the bitmap to the input size expected by the model (e.g., 224x224)
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);

        // Convert the bitmap to a byte buffer, which will be used as input for the model
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaledBitmap);

        // Prepare a 2D array to store the model's output (probabilities for each class)
        float[][] result = new float[1][LABEL_LIST.size()];

        // Run the model with the input buffer and get the output (probabilities)
        INTERPRETER.run(byteBuffer, result);

        // Sort and return the top recognition results based on confidence
        return getSortedResult(result);
    }

    // Convert the bitmap image into a byte buffer that can be fed into the TFLite model
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        // Allocate buffer to hold image data in the format required by the model
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());

        // Get the pixel values from the bitmap
        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        // Normalize the pixel values and add them to the buffer
        int pixel = 0;
        for (int i = 0; i < INPUT_SIZE; ++i) {
            for (int j = 0; j < INPUT_SIZE; ++j) {
                int val = intValues[pixel++];
                byteBuffer.putFloat((((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD); // Red
                byteBuffer.putFloat((((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);  // Green
                byteBuffer.putFloat(((val & 0xFF) - IMAGE_MEAN) / IMAGE_STD);         // Blue
            }
        }
        return byteBuffer;
    }

    // Get the sorted recognition results from the model's output
    private List<Recognition> getSortedResult(float[][] labelProbArray) {
        // Log the size of the result array (for debugging)
        Log.d("Classifier", String.format("List Size:(%d, %d, %d)", labelProbArray.length, labelProbArray[0].length, LABEL_LIST.size()));

        // Priority queue to store the results sorted by confidence
        PriorityQueue<Recognition> pq = new PriorityQueue<>(
                MAX_RESULTS,
                // Custom comparator to compare recognition confidence values
                (lhs, rhs) -> Float.compare(rhs.getConfidence(), lhs.getConfidence())
        );

        // Iterate over the label list and add results to the priority queue if confidence is above threshold
        for (int i = 0; i < LABEL_LIST.size(); ++i) {
            float confidence = labelProbArray[0][i];
            if (confidence >= THRESHOLD) {
                pq.add(new Recognition("" + i, LABEL_LIST.size() > i ? LABEL_LIST.get(i) : "Unknown", confidence));
            }
        }

        // Log the number of results (for debugging)
        Log.d("Classifier", String.format("pqsize:(%d)", pq.size()));

        // Get the top results from the priority queue and return them as a list
        List<Recognition> recognitions = new ArrayList<>();
        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());
        }
        return recognitions;
    }
}
