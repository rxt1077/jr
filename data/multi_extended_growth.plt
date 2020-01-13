set datafile separator ','
set key outside autotitle columnhead
set terminal pngcairo
set output "multi_extended_growth.png"
set title "Extended Network Growth for Multiple F Values"
set xlabel "Iterations"
set ylabel "Extended Network Size"
plot "multi_extended_growth.csv" using 1:2 with lines,\
     "multi_extended_growth.csv" using 1:3 with lines,\
     "multi_extended_growth.csv" using 1:4 with lines,\
     "multi_extended_growth.csv" using 1:5 with lines,\
     "multi_extended_growth.csv" using 1:6 with lines,\
     "multi_extended_growth.csv" using 1:7 with lines,\
     "multi_extended_growth.csv" using 1:8 with lines,\
     "multi_extended_growth.csv" using 1:9 with lines,\
     "multi_extended_growth.csv" using 1:10 with lines
