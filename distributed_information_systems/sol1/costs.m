%% Optimal View Point Concept for more Complex Models
% net.cscience.waltenspuel
% 


%% Initial Setup
%
% Clean Workspace
clc; clear all; close all;
% Set Latex as interpreter
set(0,'defaultTextInterpreter','latex');


x = [1:20];
f1 = @(x) 712*12*x+25000;
f2 = @(x) 903*12*x;
f3 = @(x) 712*12*x;
    figure()
plot(x,f1(x),'-o')
hold on

plot(x,f2(x),'-o')
grid on
title("Cost Comparison: On-Premise vs. Cloud Solution 20 years")
    xlabel('Years')
    ylabel('Cumulative Costs $')

    figure()
plot(x,f3(x),'-o')
hold on

plot(x,f2(x),'-o')
grid on
title("Cost Comparison: On-Premise vs. Cloud Solution 20 years without pre invest")
xlabel('Years')
ylabel('Cumulative Costs $')