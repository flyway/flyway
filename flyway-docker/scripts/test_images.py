import sys
import os
import subprocess


def build_non_multi_arch_standard_flyway_image(version):
    build_command = f'docker build --target flyway --pull --build-arg FLYWAY_VERSION={version} -q -f ./dockerfiles/Dockerfile .'
    print(build_command)
    return subprocess.run(build_command, capture_output=True, encoding="UTF_8", check=True, shell=True).stdout.strip()
    

if __name__ == "__main__":
    edition = sys.argv[1]
    version = sys.argv[2]
     
    images = [f'{edition}/flyway:{version}', f'{edition}/flyway:{version}-alpine', f'{edition}/flyway:{version}-azure']
    flyway_commands = ["info", "migrate", "clean -cleanDisabled=false"]
    
    if edition == "flyway":
        images[0] = build_non_multi_arch_standard_flyway_image(version)
    
    test_sql_path = os.getcwd() + "/test-sql"

    env_var_flag = ""
    if len(sys.argv) > 3:
        env_var_flag = f'-e {sys.argv[3]}={os.getenv(sys.argv[3])}'

    flyway_cli_params = "-url=jdbc:sqlite:test "
    if edition == "redgate":
        flyway_cli_params += f'-licenseKey={os.environ["FLYWAY_LICENSE_KEY"]} '
        flyway_commands.append("check -code -reportFilename=report")
        flyway_commands.append("check -changes -check.buildUrl=jdbc:sqlite:temp -reportFilename=report -cleanDisabled=false")
        
    for image in images:
        if "azure" in image:
            flyway = "flyway"
        else:
            flyway = ""
        for flyway_command in flyway_commands:
            run_command = f'docker run --rm -v "{test_sql_path}:/flyway/sql" {env_var_flag} {image} {flyway} {flyway_command} {flyway_cli_params}'
            print(run_command)
            subprocess.run(run_command, check=True, shell=True)
        