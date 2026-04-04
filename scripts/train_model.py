#!/usr/bin/env python3

from mediapipe_model_maker import gesture_recognizer

# Assuming the exported gesture data from the debug screen is placed here
data = gesture_recognizer.Dataset.from_folder(
    dirname="exported_gesture_data/",
    hparams=gesture_recognizer.HandDataPreprocessingParams()
)
train_data, test_data = data.split(0.8)
model = gesture_recognizer.GestureRecognizer.create(
    train_data=train_data,
    validation_data=test_data,
    options=gesture_recognizer.GestureRecognizerOptions(
        hparams=gesture_recognizer.HParams(
            epochs=50,
            batch_size=2,
            learning_rate=0.001
        )
    )
)
model.export_model("custom_gesture_recognizer.task")
print(model.evaluate(test_data))
