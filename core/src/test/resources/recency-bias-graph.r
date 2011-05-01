#!/usr/bin/env Rscript --vanilla
if (!library("getopt", character.only = TRUE, logical.return = TRUE)) {
  install.packages("getopt", repos = "http://lib.stat.cmu.edu/R/CRAN")
}
require("getopt")

# Setup parameters for the script
params = matrix(c(
  'help',    'h', 0, "logical",
  'input',   'i', 2, "character"
  ), ncol=4, byrow=TRUE)

# Parse the parameters
opt = getopt(params)

data <- read.csv(file=opt$input,head=TRUE,sep=",")
plot(data$t, data$exponential.mean, "l", xlab="Time", ylab="Mean", col="tomato")
lines(data$expected.exponential.mean, col="tomato4")
lines(data$uniform.mean, col="violetred")
lines(data$expected.uniform.mean, col="violetred4")
