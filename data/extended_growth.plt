set datafile separator ','
set key autotitle columnhead
unset key
set terminal pngcairo
set output "extended_growth.png"
set dgrid3d splines
set title "Extended Network Growth"
set xlabel "Iterations"
set ylabel "Extended Network Size"
plot "extended_growth.csv" using 1:2 with lines
