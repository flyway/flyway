CREATE TABLE "${schema}"."${table}" (
  "installed_rank" INTEGER NOT NULL,
  "version" VARCHAR(50),
  "description" VARCHAR(200) NOT NULL,
  "type" VARCHAR(20) NOT NULL,
  "script" VARCHAR(1000) NOT NULL,
  "checksum" INTEGER,
  "installed_by" VARCHAR(100) NOT NULL,
  "installed_on" TIMESTAMP NOT NULL DEFAULT getdate(),
  "execution_time" INTEGER NOT NULL,
  "success" BIT NOT NULL
);
ALTER TABLE "${schema}"."${table}" ADD CONSTRAINT "${table}_pk" PRIMARY KEY ("installed_rank");