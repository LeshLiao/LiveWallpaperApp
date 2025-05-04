# PaletteWall - Architecture Document

This document describes the architecture and technical details of the PaletteWall application, providing guidelines for development.

## Technology Stack

- **Programming Language**: Kotlin
- **Minimum SDK**: API 26 (Android 8.0)
- **Target SDK**: API 34 (Android 14)
- **UI Framework**: Jetpack Compose
- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Koin
- **Asynchronous Processing**: Kotlin Coroutines + Flow
- **Image Processing**: Coil, CameraX
- **Data Storage**: Room Database, DataStore
- **LiveWallpaper API**: WallpaperManager, WallpaperService

## Application Architecture

### Overall Architecture

The application adopts the MVVM architecture, organizing code according to Clean Architecture principles with the following layers:

1. **Presentation Layer**:

   - Compose UI components
   - ViewModels
   - UI state management

2. **Domain Layer**:

   - Use Cases
   - Domain Models
   - Repository Interfaces

3. **Data Layer**:
   - Repository Implementations
   - Data Sources (local and remote)
   - Model Mappers

### Module Structure

```
app/
 ├── core/
 │    ├── di/              # Dependency injection modules
 │    ├── utils/           # Common utility classes
 │    ├── extensions/      # Kotlin extension functions
 │    └── platform/        # Platform-specific utilities
 ├── data/
 │    ├── repository/      # Repository implementations
 │    ├── datasource/      # Data sources
 │    │    ├── local/      # Local data sources
 │    │    └── remote/     # Remote data sources
 │    └── mapper/          # Data mappers
 ├── domain/
 │    ├── model/           # Domain models
 │    ├── repository/      # Repository interfaces
 │    └── usecase/         # Use cases
 └── presentation/
      ├── main/            # Main interface
      ├── editor/          # Wallpaper editor
      │    ├── components/ # Reusable components
      │    └── screens/    # Editor screens
      ├── preview/         # Wallpaper preview
      ├── settings/        # Application settings
      └── wallpaper/       # Wallpaper service
```

## Key Functional Modules

### Photo Editor Module

This is the core part of the application, responsible for photo upload, editing, and composition:

1. **Photo Selection and Cropping**:

   - Using ContentResolver and MediaStore API to access device gallery
   - Using CameraX for photo capture functionality
   - Custom cropping tools for image cropping

2. **Photo Collage Editing**:

   - Canvas-based collage editor
   - Support for drag, scale, and rotate operations
   - Real-time preview

3. **Photo Filters and Effects**:
   - Using RenderScript or Compose's graphics API to process filters
   - Support for basic adjustments like brightness, contrast, saturation

### Wallpaper Service Module

Responsible for converting user-created photo collages into live wallpapers:

1. **WallpaperService Implementation**:

   - Extending Android's WallpaperService
   - Implementing Engine class to handle wallpaper rendering and user interaction

2. **Wallpaper Rendering**:

   - Using OpenGL ES or Compose's GraphicsLayer for rendering
   - Handling different screen sizes and orientations

3. **Interaction Handling**:
   - Touch event processing
   - Adjusting wallpaper display based on user gestures

### Animation System Module

Responsible for implementing the dynamic movement of images in the live wallpaper:

1. **Animation Engine**:

   - Using ValueAnimator or Compose Animation API to handle image transitions
   - Implementing smooth movement patterns from edges to center

2. **Animation Controls**:

   - Managing animation speed and timing
   - Handling animation triggers (automatic or user-initiated)

3. **Performance Optimization**:
   - Efficiently rendering animations without degrading device performance
   - Implementing frame rate control for battery optimization

### Data Persistence

Managing user-created wallpapers and settings:

1. **Wallpaper Storage**:

   - Using Room database to store wallpaper metadata
   - Using file system to store wallpaper image resources

2. **User Preferences**:
   - Using DataStore to store user settings and preferences

## Key Workflows

### Wallpaper Creation Workflow

1. User uploads photos or selects them from device
2. Photos undergo initial processing (cropping, resizing)
3. User edits photo collage (replacement, rotation, adjustment)
4. Preview wallpaper effect
5. Save as live wallpaper
6. Apply to device desktop

### Photo Replacement Workflow

1. User selects the photo to replace
2. System opens media selector
3. User selects new photo
4. System processes new photo (crops to appropriate size)
5. UI updates to show the replaced effect

## Performance Considerations

- Using caching mechanisms to reduce memory consumption during image processing
- Processing images on background threads
- Lazy loading and pagination for loading large numbers of images
- Optimizing live wallpaper for battery usage

## Testing Strategy

- **Unit Tests**: Testing domain and data layer logic
- **UI Tests**: Using Compose UI testing
- **Integration Tests**: Testing integration between modules
- **Performance Tests**: Testing wallpaper rendering performance and battery consumption

## First Version Implementation Plan

The first version (MVP) will focus on implementing basic photo collage functionality:

1. Implement basic UI framework with a template featuring two default images
2. Develop image selection mechanism with focus indicator (red border)
3. Implement photo selection and uploading for replacement
4. Develop single and batch replacement functionality
5. Implement rotation and orientation adjustment features
6. Create animation system for dynamic image movement:
   - One image moving from left to center
   - Another image moving from right to center
7. Create basic wallpaper service
8. Complete wallpaper saving and application functionality
