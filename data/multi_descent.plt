set datafile separator ','
set key autotitle columnhead
unset key
set terminal pngcairo

set output "multi_descent_b.png"
set title "B vs. f for a 100 Node Network"
set xlabel "f (following list size)"
set ylabel "B (growth rate)"
plot "multi_descent.csv" using 1:2 with lines

set output "multi_descent_m.png"
set title "M vs. f for a 100 Node Network"
set xlabel "f (following list size)"
set ylabel "M (time of maximum growth)"
plot "multi_descent.csv" using 1:3 with lines

