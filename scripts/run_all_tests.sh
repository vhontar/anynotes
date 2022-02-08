print_blue() {
  # shellcheck disable=SC2059
  printf "\e[1;34m$1\e[0m"
}

print_blue "\n\nStarting Firestore local emulator..."
firebase emulators:exec --only firestore "/Users/vladyslavhontar/Work/android/AnyNotes/scripts/instrumentation_unit_tests.sh"