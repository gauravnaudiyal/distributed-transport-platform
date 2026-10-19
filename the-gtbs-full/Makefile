SHELL := /bin/bash

.PHONY: help init submodules submodules-remote submodules-main up down restart status logs logs-% jwt smoke fault recover ps

help:
	@echo "GTBS meta repo commands"
	@echo ""
	@echo "Setup:"
	@echo "  make init               # init submodules to recorded SHAs"
	@echo "  make submodules         # init + checkout/pull origin/main in each submodule"
	@echo "  make submodules-remote  # move submodules to latest tracked remote commit"
	@echo "  make submodules-main    # alias of make submodules"
	@echo ""
	@echo "Stack:"
	@echo "  make up                 # build and start full local stack"
	@echo "  make down               # stop stack"
	@echo "  make restart            # down then up"
	@echo "  make status             # docker compose status"
	@echo "  make ps                 # alias of status"
	@echo "  make logs               # follow all logs"
	@echo "  make logs-booking-service"
	@echo ""
	@echo "Demo:"
	@echo "  make jwt                # export sample DEMO_JWT command"
	@echo "  make smoke              # run demo checks (requires DEMO_JWT)"
	@echo ""
	@echo "Faults:"
	@echo "  make fault SCENARIO=redis-down"
	@echo "  make recover SCENARIO=redis-down"

init:
	git submodule update --init --recursive

submodules:
	git submodule update --init --recursive
	git submodule foreach --recursive 'git fetch origin && git checkout main && git pull --ff-only origin main'

submodules-remote:
	git submodule sync --recursive
	git submodule update --init --recursive --remote

submodules-main:
	$(MAKE) submodules


up:
	./scripts/stack.sh up

down:
	./scripts/stack.sh down

restart: down up

status ps:
	./scripts/stack.sh status

logs:
	./scripts/stack.sh logs

logs-%:
	./scripts/stack.sh logs $*

jwt:
	@echo 'export DEMO_JWT="$$(./scripts/set-demo-jwt.sh --raw demo-user 3600)"'

smoke:
	./scripts/run-demo-checks.sh

fault:
	@if [[ -z "$$SCENARIO" ]]; then echo "Set SCENARIO, e.g. make fault SCENARIO=redis-down"; exit 1; fi
	./scripts/stack.sh fault "$$SCENARIO"

recover:
	@if [[ -z "$$SCENARIO" ]]; then echo "Set SCENARIO, e.g. make recover SCENARIO=redis-down"; exit 1; fi
	./scripts/stack.sh recover "$$SCENARIO"
