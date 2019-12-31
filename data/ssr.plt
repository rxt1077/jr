set datafile separator ','
set key autotitle columnhead
unset key
set terminal png
set output "ssr.png"
set view 70, 330                  
set dgrid3d splines
set title "Cost Function"       
set ytics 200
set ylabel "m" offset 0,-1,0
set xlabel "b" offset 0,-1,0
set xtics 0.02
splot "ssr.csv" u 1:2:3 with lines
