#!/vendor/bin/sh

## Set the hardware keyboard variant
layout=$(cat /mnt/vendor/persist/keyboard/layout)
setprop sys.keyboard.layout ${layout}
