# Simple R script to fit a sigmoid and write a new column of fitted data to
# a CSV

sigmoid = function(params, x) {
	A <- 30 # params[1] the lower asymptote
	K <- 100 # params[2] # the upper asymptote
	B <- params[3] # the growth rate
	M <- params[4] # the time of maximum growth

	A + (K - A) / (1 + exp(-B*(x-M)))
}

data = read.csv("multi_extended_growth.csv")
x <- data$iterations
y1 <- data$ewok
fitmodel <- nls(y1 ~ 30 + (100 - 30) / (1 + exp(-B*(x - M))),
#		start=list(A=10,K=100,B=0.01,M=500))
		start=list(B=0.01,M=250))
print(fitmodel)
params = coef(fitmodel)
y2 <- sigmoid(params, x)
#plot(x, y1, col='blue')
#points(x, y2, col='red')
output_data <- data.frame(iterations=x, avg_extended_size=y1, fit=y2)
write.csv(output_data, "extended_setup_fit.csv", row.names=FALSE)
