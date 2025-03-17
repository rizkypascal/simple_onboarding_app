# Simple Onboarding App

## Prerequisite
* [Android Studio](https://developer.android.com/studio?gad_source=1&gclid=Cj0KCQjwkN--BhDkARIsAD_mnIr88Ek8p8RrZIv8coZXI7KtxNn3hBTEk8YNv5GCm8yf4Bxy__5EJmkaArXWEALw_wcB&gclsrc=aw.ds)
* Clone this repository (git@github.com:rizkypascal/simple_onboarding_app.git)

## Build & Run
1. Open Android Studio and open the cloned repository, it will automatically build the application
2. Open gradle.properties and fill these following environment variables (do not forget to remove these values while pushing code)
    ```groovy
    BASE_URL=
    FUEL_URL=
    API_KEY=
    API_SECRET=
    ```
3. Sync the build again
4. Go to View > Tool Windows > Device Manager, add your virtual device there
5. Then go to Run > Run 'app'

## Documentation
1. Sequence Diagram -> open sequence_diagram.txt, copy the content, go to sequencediagram.org, paste the copied content there

## User Interfaces
1. Main Menu
2. Adjust Brightness Tutorial
3. Scanning Tips
4. Scanning Process