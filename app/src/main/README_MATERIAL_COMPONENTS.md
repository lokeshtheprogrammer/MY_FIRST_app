# Material Components 3 Implementation Guide

## Overview
This project has been updated to use Material Components 3 (Material You) design system. This README provides guidance on how to use the Material Components in the Nutrifill app.

## Theme Configuration
The app uses a custom Material 3 theme defined in `themes.xml` for both light and dark modes. The theme inherits from `Theme.Material3.DayNight`.

## Available Styles

### Buttons
- **Standard Button**: `Widget.Nutrifill.Button`
  ```xml
  <com.google.android.material.button.MaterialButton
      style="@style/Widget.Nutrifill.Button"
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      android:text="Button Text" />
  ```

- **Outlined Button**: `Widget.Nutrifill.Button.OutlinedButton`
  ```xml
  <com.google.android.material.button.MaterialButton
      style="@style/Widget.Nutrifill.Button.OutlinedButton"
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      android:text="Outlined Button" />
  ```

- **Text Button**: `Widget.Nutrifill.Button.TextButton`
  ```xml
  <com.google.android.material.button.MaterialButton
      style="@style/Widget.Nutrifill.Button.TextButton"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:text="Text Button" />
  ```

### Cards
- **Elevated Card**: `Widget.Nutrifill.Card`
  ```xml
  <com.google.android.material.card.MaterialCardView
      style="@style/Widget.Nutrifill.Card"
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      android:layout_margin="8dp">
      <!-- Card content -->
  </com.google.android.material.card.MaterialCardView>
  ```

### Text Fields
- **Outlined Text Field**: `Widget.Nutrifill.TextInputLayout`
  ```xml
  <com.google.android.material.textfield.TextInputLayout
      style="@style/Widget.Nutrifill.TextInputLayout"
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      android:hint="Enter text">
      <com.google.android.material.textfield.TextInputEditText
          android:layout_width="match_parent"
          android:layout_height="wrap_content" />
  </com.google.android.material.textfield.TextInputLayout>
  ```

### Bottom Navigation
- **Bottom Navigation**: `Widget.Nutrifill.BottomNavigation`
  ```xml
  <com.google.android.material.bottomnavigation.BottomNavigationView
      style="@style/Widget.Nutrifill.BottomNavigation"
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      app:menu="@menu/bottom_navigation_menu" />
  ```

## Color System
The Material 3 color system uses the following color roles:

- **Primary**: Main brand color (`brand_primary`)
- **On Primary**: Color for text/icons on primary color (`white`)
- **Primary Container**: Color for containers with primary emphasis (`brand_accent`)
- **On Primary Container**: Color for text/icons on primary containers (`text_primary`)

- **Secondary**: Secondary brand color (`brand_secondary`)
- **On Secondary**: Color for text/icons on secondary color (`white`)
- **Secondary Container**: Color for containers with secondary emphasis (`teal_100`)
- **On Secondary Container**: Color for text/icons on secondary containers (`text_primary`)

- **Tertiary**: Tertiary brand color (`teal_500`)
- **Surface**: Background color for components (`card_background`)
- **On Surface**: Color for text/icons on surface (`text_primary`)

## Migration Tips

1. Replace standard Android widgets with Material Components:
   - `Button` → `MaterialButton`
   - `CardView` → `MaterialCardView`
   - `EditText` → `TextInputLayout` with `TextInputEditText`

2. Apply the predefined styles to maintain consistent design:
   - Use `style="@style/Widget.Nutrifill.Button"` instead of custom attributes
   - Leverage Material theming for automatic dark mode support

3. For custom components, use the Material color system attributes:
   - `?attr/colorPrimary`, `?attr/colorOnPrimary`, etc.
   - This ensures theme consistency across the app

## Example Usage

```xml
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <com.google.android.material.card.MaterialCardView
        style="@style/Widget.Nutrifill.Card"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="8dp">
        
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">
            
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Card Title"
                android:textAppearance="?attr/textAppearanceHeadlineSmall"/>
                
            <com.google.android.material.textfield.TextInputLayout
                style="@style/Widget.Nutrifill.TextInputLayout"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:hint="Enter information">
                
                <com.google.android.material.textfield.TextInputEditText
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />
            </com.google.android.material.textfield.TextInputLayout>
            
            <com.google.android.material.button.MaterialButton
                style="@style/Widget.Nutrifill.Button"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:text="Submit" />
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>
</LinearLayout>
```