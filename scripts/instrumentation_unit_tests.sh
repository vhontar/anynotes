print_red() {
  # shellcheck disable=SC2059
  printf "\e[1;31m$1\e[0m"
}
print_green() {
  # shellcheck disable=SC2059
  printf "\e[1;32m$1\e[0m"
}
print_yellow() {
  # shellcheck disable=SC2059
  printf "\e[1;33m$1\e[0m"
}
print_blue() {
  # shellcheck disable=SC2059
  printf "\e[1;34m$1\e[0m"
}

print_blue "\n\nStarting..."
print_blue "\n\ncd into working directory..."
# shellcheck disable=SC2164
cd "/Users/vladyslavhontar/Work/android/AnyNotes/"
print_blue "\n\nrun unit tests"
./gradlew test
print_green "\n\nUnit tests are completed!"
print_blue "\n\nrun androidTests..."
./gradlew connectedAndroidTest
print_green "\n\nAndroid tests are completed!"
print_yellow "\n\nAll tests completed!"