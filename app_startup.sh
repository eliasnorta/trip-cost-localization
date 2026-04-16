#!/bin/bash
set -e

sleep 2
cd /app

# Run the JavaFX app through Maven so dependencies and module setup are handled
export DISPLAY=:1
mvn -q javafx:run


