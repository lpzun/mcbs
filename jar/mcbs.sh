################################################################################
#  NOTE: This is an shell script for bopp preprocessor. It aims at translating# 
#  SatAbs BP to Our BP: mainly renaming labels                                #
#                                                                             #
#  INPUT  : a SatAbs Boolean program file                                     #
#  OUTPUT : a Boolen progran with out format                                  #
################################################################################
#  !/bin/bash                                                                 # 
JAVA=java
JAR_FLAG=-jar
RUNNABLE=mcbs.jar         # Preprocessor: SatAbs BP -> Our BP

################################################################################
# The following is how to redirect preprocessor's output to BoPP and output   # 
# the control flow grah and WP into a file                                    #
################################################################################
$JAVA $JAR_FLAG $RUNNABLE $@

