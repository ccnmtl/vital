.PHONY: setup autogen bohagan common emattes eddie sky jonah dev test prod



setup:
	scripts/setup.sh


vitalrelease:
	scripts/autogen_properties.sh $@

bohagan: 
	scripts/autogen_properties.sh $@

common: 
	scripts/autogen_properties.sh $@

dev:
	scripts/autogen_properties.sh $@

dev5:
	scripts/autogen_properties.sh $@

emattes:
	scripts/autogen_properties.sh $@

eddie:
	scripts/autogen_properties.sh $@

ejucovy:
	scripts/autogen_properties.sh $@

jonah:
	scripts/autogen_properties.sh $@

mark:
	scripts/autogen_properties.sh $@

prod:
	scripts/autogen_properties.sh $@

prod5:
	scripts/autogen_properties.sh $@

sky:
	scripts/autogen_properties.sh $@

test:
	scripts/autogen_properties.sh $@

zarina:
	scripts/autogen_properties.sh $@

platypus:
	scripts/autogen_properties.sh $@