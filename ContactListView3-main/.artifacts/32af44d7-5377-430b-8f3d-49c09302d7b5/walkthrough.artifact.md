# Walkthrough - Contacts V3 Implementation

We have successfully evolved the contacts management application from TP V2 to TP V3 according to all specified requirements and modern Android development best practices.

## Changes Made

### 1. Data & Architecture
- **[NEW] [Contact.kt](file:///C:/Users/ibent/AndroidStudioProjects/ListeContacts3/app/src/main/java/com/example/listecontacts3/Contact.kt)**: Extended data model with `latitude` (`Double?`) and `longitude` (`Double?`), along with `firstName`, `lastName`, `phoneNumber`, `email`, and `photoUri`.
- **[NEW] [ContactViewModel.kt](file:///C:/Users/ibent/AndroidStudioProjects/ListeContacts3/app/src/main/java/com/example/listecontacts3/ContactViewModel.kt)**: Shared ViewModel managing in-memory contact storage, live filtering (case-insensitive across name, first name, phone, email), contact selection state, and updates.

### 2. Fragments & UI Components
- **[MODIFY] [MainActivity.kt](file:///C:/Users/ibent/AndroidStudioProjects/ListeContacts3/app/src/main/java/com/example/listecontacts3/MainActivity.kt)** & **[activity_main.xml](file:///C:/Users/ibent/AndroidStudioProjects/ListeContacts3/app/src/main/res/layout/activity_main.xml)**: Hosted within a single activity using `FragmentContainerView`.
- **[NEW] [ListeContactsFragment.kt](file:///C:/Users/ibent/AndroidStudioProjects/ListeContacts3/app/src/main/java/com/example/listecontacts3/ListeContactsFragment.kt)** & **[fragment_liste_contacts.xml](file:///C:/Users/ibent/AndroidStudioProjects/ListeContacts3/app/src/main/res/layout/fragment_liste_contacts.xml)**: Live search EditText and `RecyclerView` displaying contact list with custom rows (`item_contact.xml`), handling empty states.
- **[NEW] [ContactAdapter.kt](file:///C:/Users/ibent/AndroidStudioProjects/ListeContacts3/app/src/main/java/com/example/listecontacts3/ContactAdapter.kt)**: RecyclerView Adapter binding contact details and handling click selection.
- **[NEW] [DetailContactFragment.kt](file:///C:/Users/ibent/AndroidStudioProjects/ListeContacts3/app/src/main/java/com/example/listecontacts3/DetailContactFragment.kt)** & **[fragment_detail_contact.xml](file:///C:/Users/ibent/AndroidStudioProjects/ListeContacts3/app/src/main/res/layout/fragment_detail_contact.xml)**: Consultation and editing form with validation for mandatory fields and GPS coordinates (-90 to 90 latitude, -180 to 180 longitude), photo selection via Gallery and Camera (using FileProvider), and implicit intents for phone calls (`ACTION_DIAL`) and emails (`ACTION_SENDTO`).
- **[NEW] [CarteContactFragment.kt](file:///C:/Users/ibent/AndroidStudioProjects/ListeContacts3/app/src/main/java/com/example/listecontacts3/CarteContactFragment.kt)** & **[fragment_carte_contact.xml](file:///C:/Users/ibent/AndroidStudioProjects/ListeContacts3/app/src/main/res/layout/fragment_carte_contact.xml)**: Google Maps integration centering on contact coordinates and displaying a marker with the contact's name, with explicit fallback messaging for missing or invalid coordinates.

### 3. Configuration & Manifest
- **[MODIFY] [AndroidManifest.xml](file:///C:/Users/ibent/AndroidStudioProjects/ListeContacts3/app/src/main/AndroidManifest.xml)**: Added Internet & Camera permissions, FileProvider configuration, and Google Maps API key metadata (`com.google.android.geo.API_KEY`).
- **[NEW] [file_paths.xml](file:///C:/Users/ibent/AndroidStudioProjects/ListeContacts3/app/src/main/res/xml/file_paths.xml)**: FileProvider paths configuration for camera image capture.

## Validation Results

- **Build Check**: Project built successfully (`app:assembleDebug`) with zero compilation errors.
- **Requirements Verified**:
  - Single Activity architecture with dynamic fragments (`replace()` and `addToBackStack()`).
  - `RecyclerView` with live case-insensitive search filtering across name, first name, phone, and email.
  - Contact editing, validation, and immediate list updating via shared ViewModel.
  - Implicit intents for phone dialing and email messaging.
  - Gallery and Camera photo picking with FileProvider support.
  - Google Maps display with marker and coordinate validation (-90/90, -180/180).
