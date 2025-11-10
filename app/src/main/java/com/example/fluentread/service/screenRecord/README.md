# Screen Recording Flow

This module provides the Android-side implementation that powers FluentRead's screen recording feature. It coordinates user permission flows, binds to the foreground recording service, and manages the MediaProjection / MediaRecorder lifecycle.

## Key Components

- `RecordingActivity`: Lightweight activity that boots the service, requests permissions, and relays start/stop actions.
- `ScreenRecorder`: Foreground service that owns MediaProjection, MediaRecorder, notifications, and lifecycle state.
- `AppScreenRecorder`: Interface contract exposing the operations and callbacks for screen recording.
- `models/`: Supporting data structures (`ScreenRecordState`, `ScreenRecordResult`, `ScreenRecordConfig`).

## High-Level Flow

```
        +--------------------+
        |  UI / Toggle Layer |
        +---------+----------+
                  |
                  | Intent (ACTION_START / STOP / CANCEL)
                  v
        +---------+----------+
        | RecordingActivity  |
        +---------+----------+
                  |
                  | bindService + startService
                  v
        +---------+----------+    permission granted
        |   ScreenRecorder   |<--------------------+
        +---------+----------+                     |
                  |                                |
                  | startNotificationForeground()   |
                  |                                |
                  v                                |
        +---------+----------+                     |
        | MediaProjection    |   createScreenCaptureIntent()
        +---------+----------+                     ^
                  |                                |
                  | provides Capture Intent        |
                  v                                |
        +---------+----------+                     |
        | MediaRecorder      |   ActivityResultLauncher.launch()
        +---------+----------+                     |
                  |                                |
                  +-----> Recording States ---------+
```

## Sequence Summary

1. **Trigger**: UI logic issues an intent aimed at `RecordingActivity` with the desired action (`ACTION_START`, `ACTION_STOP`, or `ACTION_CANCEL`).
2. **Activity Startup**: `RecordingActivity` starts/binds `ScreenRecorder` and inspects the incoming action.
   - For `ACTION_START`, it checks storage (and future audio) permissions, then launches the media projection permission prompt.
   - For `ACTION_STOP`, it forwards the stop intent to the service and exits.
3. **Permission Result**: When the user grants capture permission, the resulting intent should be supplied to the service (currently a TODO in `onActivityResult`).
4. **Service Start**: `ScreenRecorder` moves into foreground mode, builds a notification channel, and calls `startRecording`.
5. **Recorder Setup**:
   - Requests a cache file via `FileHelper`.
   - Acquires a `MediaProjection` instance using the permission intent.
   - Initializes `MediaRecorder` with 1080p/30fps video + AAC audio and prepares it.
   - Starts the recorder and updates `ScreenRecordState` to `RECORDING`.
6. **Lifecycle Control**: The service responds to pause/resume/stop by delegating to `MediaRecorder`, tracking durations, and cleaning up resources.
7. **Completion**: `stopRecording` returns a `ScreenRecordResult` (file path, duration, size) and clears state before stopping the foreground service.

## State Machine

```
IDLE -> (initMediaProjection) -> PREPARED -> (mediaRecorder.start) -> RECORDING
RECORDING -> (pause) -> PAUSED -> (resume) -> RECORDING
RECORDING -> (stopRecording) -> SAVING (implicit) -> IDLE
PREPARED -> (release / error) -> IDLE
```

## Integration Notes

- `RecordingActivity` currently does not forward the `ActivityResult` data back to the service. Implementing that wiring is required for end-to-end recording.
- For Android 13+ you may need runtime consent for audio and foreground service types (UPDATE: check `POST_NOTIFICATIONS` if targeting API 33+).
- Make sure the service is declared with `foregroundServiceType="mediaProjection"` in the manifest.
- The cache output file is generated under the app-specific cache directory; persist or upload it before the system reclaims the space.

## Testing Checklist

- Launch recording from the overlay toggle and ensure the permission dialogue appears only once.
- Verify notification presence while recording and removal after stop/cancel.
- Inspect saved file metadata (resolution, bitrate, duration) to confirm `MediaRecorder` configuration.
- Handle orientation changes gracefully—`MediaProjection` callbacks notify about size and visibility changes.

