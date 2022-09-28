# Solaign
Created by Alex Kranias & Ayush Pai.

Solaign is a mobile tool to maximize the power output of residential solar panel instillations. Most residential solar panels are fixed in place when instialled, and often times when installed they are not oriented optimally, leading to much of the sunlight they could capture over the course of months, years, or even decades to be lost to poor panel alignment. Solaign solves this by using GPS and climate data in tandem with solar positioning algorithims to calculate the best alignment for a given solar panel installation.

## Purpose
After writing my IB Physics Extended Essay on ["Maximizing the Energy Production of a Fixed-Tilt Solar Cell"](https://drive.google.com/file/d/1nEn__nq9saxK5wogBIKF1KFQjA58j0wy/view?usp=sharing), I felt that I could take the methodology in my paper and generaize it for all possible time periods and locations in the world, and package it all into a user friendly app that could be used by solar panel instillation companies and individual solar panel owners to have a tool to assess the effectivness of their solar instillation. The hope is that through this app, we can help get the most out of the millions of residential solar panels installed today, and the millions more yet to be installed. I encourage you to [learn more about residential solar panels in the United States](https://usafacts.org/articles/how-much-solar-energy-do-homes-produce/#:~:text=Since%20then%2C%20the%20number%20of,solar%20systems%20in%20the%20US).


## How the App Works
Solaign make's use of J.J. Michalsky's Solar Position Algorithim (1988) and preliminary software written by Martin Rymes of the National Renewable Energy Laboratory (March 1998) translating much of Michalsky's algorithim into C++, which I translated to Java, and represents the sun-panel system through vector projection while using equations describing the atmosphere's effect on solar irradiance (solar energy) to calculate the total energy captured by a solar panel over a given time period. The key is that by using gradient descent, our algorithim is able to quickly and efficently calculate the optimal position needed of a solar panel to maximize how much energy it captures over the user-inputed time period for the user's specific location on Earth. With this information, the app presents it to users in simple set of instructions for them to position their panels at the optimal position just calculated. While this app is currently (8/3/2022) extremely accurate for locations with little to no rain or cloud cover throughout the year, I am currently searching for APIs and/or scientific databases to feed our algorithim hourly "cloudiness" data for every locaiton on Earth in addition to finding proper equations that accurately describe the effect of cloud cover on the sun's intensity (solar irradiance) in order to make Solaign one of the most accurate and accessible solar positioning tools availible.
