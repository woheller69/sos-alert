# Contributing to SOS Alerter

First of all, thank you for your interest in contributing to **SOS Alerter**! Contributions from the community help make this application safer, more reliable, and better suited for everyone. We welcome contributions of all types.

---

## 💡 Ways to Contribute

* **Bug Reports**: Find and document reproducible bugs in the app.
* **Feature Requests**: Propose design ideas or request helpful safety features.
* **Documentation**: Fix typos, clarify setup steps, or improve guides.
* **Testing**: Test the app under different Android versions, devices, and layouts.
* **UI Improvements**: Optimize Material Design layouts, accessibility, and contrast.
* **Code Contributions**: Implement code changes, bug fixes, or optimizations directly.

---

## 🛠 Development Setup

To prepare your local system for code contributions:

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/dhilipmpms/SOS-alerter.git
   cd SOS-alerter
   ```
2. **Install Android Studio**: Download and install the latest stable version of Android Studio.
3. **Open Project**: Launch Android Studio and choose **Open**, selecting the root directory of the repository.
4. **Build & Sync**: Let Gradle synchronize dependencies and build the application cleanly.
5. **Run Application**: Connect an Android device or emulator and run the app to start developing.

---

## 🌿 Branch Naming Guidelines

When creating a new branch, please follow this naming convention:

* **Features**: `feature/<feature-name>` (e.g., `feature/wearos-support`)
* **Bug Fixes**: `bugfix/<bug-name>` (e.g., `bugfix/strobe-timer-crash`)
* **Documentation**: `docs/<documentation-change>` (e.g., `docs/add-installation-guide`)

---

## 📝 Commit Messages

We encourage clear, readable, and structured commit messages. Follow these examples:

* `feat: add emergency calling`
* `fix: resolve startup crash`
* `docs: update README`
* `style: align settings checkboxes`

---

## 🚀 Pull Requests

Before submitting your pull request, ensure it meets the following criteria:

* **Builds Successfully**: The project must compile cleanly without build errors. Run `./gradlew assembleDebug` to test compilation locally.
* **Style Guidelines**: Ensure your code is clean, readable, and maintains the style of the surrounding code.
* **Explain Changes**: Write a brief summary in the description explaining the rationale behind your changes.
* **UI Screenshots**: If your PR makes changes to UI components, please attach before/after screenshots or recordings.

---

## 🐛 Reporting Bugs

When reporting a bug, please use the issue tracker and include the following details:

* **Android Version**: (e.g., Android 13 / API 33)
* **Device Model**: (e.g., Google Pixel 7a)
* **Steps to Reproduce**: Detailed list of actions that cause the bug.
* **Expected Behavior**: What should happen under correct operation.
* **Actual Behavior**: Description of the crash, freeze, or incorrect state.

---

## 🤝 Code of Conduct

Help us maintain a positive and supportive community environment:
* **Be respectful** and considerate of others.
* **Support new contributors** and help them learn.
* **Encourage FOSS collaboration** to advance public safety apps.

---

## 📄 License Agreement

By contributing to SOS Alerter, you agree that your contributions will be released under the **GNU GPL-3.0 License**.
