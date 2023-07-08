# Specification: SoulScript - Bible App with Verse Explanations
The proposed app utilizes the OpenAI ChatGPT API to generate relevant Bible verses and their explanations based on user input. It is a utility application designed to provide guidance and comfort to users in their journey of faith. Ultimately, the app serves as a solution for individuals seeking to deepen their understanding of the Bible and find solace in its teachings.
When the user enters their keywords or problem description and presses the search button, an API call is made and it returns a bible verse with an explanation relevant to what keywords were entered, users can then save these verses+explanation for later viewing. Firebase will be used for user authentication and saving verses.

## Features:
Sharing: Users can share verses with other individuals.
Offline Access: Users can access saved Bible verses and explanations even when offline.
Users can change passwords *new feature*
Users can get a password reset email *new feature*
Layout:
Login page:
    - Users can enter their email and password to sign in or go to the registration page/forgot password page 

Registration:
    - Users can enter an email and enter and confirm their password to register

Forgot Password Page:
    - Users can enter an email to get a password reset link *new feature*

Home Screen:
    - The app's home screen will have a simple search bar and a search button to search for relevant Bible verses and explanations.
    - A "Recommend" button will be displayed below the search bar that will provide personalised recommendations based on the user's bookmarked verses. *changed feature*
    - Button to open the settings page *changed feature*

Bible Verse and Explanation Page:
    - When a user searches for a Bible verse, the app will display a page with the selected Bible verse and its explanation.
    - A bookmark button will be displayed on the page that allows the user to save the verse and its explanation for later viewing.

Bookmarks Page:
    -The bookmarks page will list all the saved Bible verses and explanations.
    - The user can click on a bookmarked verse to view its explanation.
Notification Settings:
    - The notification settings page will allow the user to turn on or off daily notifications with new Bible verses *changed feature*




Changes:
    - Recommends verses based on bookmarks, not search history
    - The button on the home page takes you to the general settings page, not a specific notification page
    - Notifications don’t offer the option for bible trivia or to select the frequency.

New Implementation:
    - Can change password
    - Can get a password reset email
Not Implemented:
    - Shared verses can’t be saved.
