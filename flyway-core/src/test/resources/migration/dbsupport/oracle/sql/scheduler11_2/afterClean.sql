--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

declare
  l_owner varchar2(128) := SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA');
begin
  for r in (select * from all_scheduler_credentials where owner = l_owner) loop
    dbms_scheduler.drop_credential(
      credential_name => '"' || l_owner || '"."' || r.credential_name || '"'
    );
  end loop;
end;
/

