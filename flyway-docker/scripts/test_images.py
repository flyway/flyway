import sys
import os
import subprocess


def build_non_multi_arch_standard_flyway_image(version, target, folder=""):
    build_command = f'docker build --target {target} --pull --build-arg FLYWAY_VERSION={version} -q -f ./dockerfiles/{folder}/Dockerfile .'
    print(build_command)
    return subprocess.run(build_command, capture_output=True, encoding="UTF_8", check=True, shell=True).stdout.strip()
    

if __name__ == "__main__":
    edition = sys.argv[1]
    version = sys.argv[2]
    linux_only = sys.argv[3] == "True"
    
    images = [f'{edition}/flyway:{version}']
    if not linux_only:
        images.append(f'{edition}/flyway:{version}-alpine')
        images.append(f'{edition}/flyway:{version}-azure')
    
    if edition == "flyway":
        images[0] = build_non_multi_arch_standard_flyway_image(version, edition)
        if not linux_only:
            images[1] = build_non_multi_arch_standard_flyway_image(version, edition, "alpine")
        images.append(build_non_multi_arch_standard_flyway_image(version, "mongo"))
    else:
        images.append(f'{edition}/flyway:{version}-mongo')

    flyway_commands = ["info", "migrate", "clean -cleanDisabled=false"]
    
    test_sql_path = os.getcwd() + "/test-sql"

    env_var_flag = ""
    if len(sys.argv) > 4:
        env_var_flag = f'-e {sys.argv[4]}={os.getenv(sys.argv[4])}'

    flyway_cli_params = "-url=jdbc:sqlite:test "
    if edition == "redgate":
        flyway_cli_params += f'-licenseKey={os.environ["FLYWAY_LICENSE_KEY"]} '
        flyway_commands.append("check -code -reportFilename=report")
        flyway_commands.append("check -changes -check.buildUrl=jdbc:sqlite:temp -reportFilename=report -cleanDisabled=false")
        
    for i, image in enumerate(images):
        if "azure" in image:
            flyway = "flyway"
        else:
            flyway = ""
        for flyway_command in flyway_commands:
            run_command = f'docker run --rm -v "{test_sql_path}:/flyway/sql" {env_var_flag} {image} {flyway} {flyway_command} {flyway_cli_params}'
            print(run_command)
            subprocess.run(run_command, check=True, shell=True)

        if (edition == "redgate") or (i == len(images) - 1):
            flyway_cli_params_mongo = f'-url={os.getenv("MONGO_CONNECTION_DETAILS")} -sqlMigrationSuffixes=".js" -cleanDisabled=false '
            for flyway_command in ["clean", "info", "migrate"]:
                run_command = f'docker run --rm -v "{test_sql_path}:/flyway/sql" {env_var_flag} {image} {flyway} {flyway_command} {flyway_cli_params_mongo}'
                print(run_command)
                subprocess.run(run_command, check=True, shell=True)
        