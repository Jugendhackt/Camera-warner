# Camera-warner

## Goals
This app's aim is to warn you if you get too near a cctv.

## Description
You can set the radius in which you will be notified in the settings as well as the Source for the camera's to use (thought there's only 1 available currently). The app will show a map with all camera's near you when you open it. The cctv markers are clustered to improve the performance. They will be replaced by a heatmap in the future.

## Building
- Use the compiled apks from the downlaod section
- Build the app on your own

  Note: You have to replace the google maps api key with you own. Just set the value in the app's build.gradle in android/manifestPlaceholders. See the [Google Maps Documentation](https://developers.google.com/maps/documentation/android-api/signup?hl=de) for how to get an api key.
