import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.ldap.LdapContext;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

public class MemberOfAuth{

	private final String domainName;
    private static final String MEMBER_OF = "memberOf";
    private static final String[] attrIdsToSearch = new String[] { MEMBER_OF };
    public static final String SEARCH_BY_SAM_ACCOUNT_NAME = "(sAMAccountName=%s)";
    public static final String SEARCH_GROUP_BY_GROUP_CN = "(&(objectCategory=group)(cn={0}))";
    private static final String CONTEXT_FACTORY_CLASS = "com.sun.jndi.ldap.LdapCtxFactory";
    private String ldapServerUrls[];
    private int lastLdapUrlIndex;
    
    public MemberOfAuth(String domainName){
    	this.domainName = domainName.toUpperCase();
    	 
        try{
            ldapServerUrls = nsLookup(domainName);
        }catch(Exception e){
            e.printStackTrace();
        }
        lastLdapUrlIndex = 0;
    }
    
    private static String[] nsLookup(String argDomain) throws Exception {
        try{
            Hashtable<Object, Object> env = new Hashtable<Object, Object>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
            env.put("java.naming.provider.url", "dns:");
            DirContext ctx = new InitialDirContext(env);
            Attributes attributes = ctx.getAttributes(String.format("_ldap._tcp.%s", argDomain), new String[] { "srv" });
            // try thrice to get the KDC servers before throwing error
            for(int i = 0; i < 3; i++){
                Attribute a = attributes.get("srv");
                if(a != null){
                    List<String> domainServers = new ArrayList<String>();
                    NamingEnumeration<?> enumeration = a.getAll();
                    while (enumeration.hasMoreElements()) {
                        String srvAttr = (String) enumeration.next();
                        // the value are in space separated 0) priority 1)
                        // weight 2) port 3) server
                        String values[] = srvAttr.toString().split(" ");
                        domainServers.add(String.format("ldap://%s:%s", values[3], values[2]));
                    }
                    String domainServersArray[] = new String[domainServers.size()];
                    domainServers.toArray(domainServersArray);
                    return domainServersArray;
                }
            }
            throw new Exception("Unable to find srv attribute for the domain " + argDomain);
        }catch (NamingException exp) {
            throw new Exception("Error while performing nslookup. Root Cause: " + exp.getMessage(), exp);
        }
    }
    
    public boolean isMemberOf(String groupName, String username, String password) throws LoginException{
    	 if(ldapServerUrls == null || ldapServerUrls.length == 0) {
             throw new AccountException("Unable to find ldap servers");
         }
         if(username == null || password == null || username.trim().length() == 0 || password.trim().length() == 0) {
             throw new FailedLoginException("Username or password is empty");
         }
         int retryCount = 0;
         int currentLdapUrlIndex = lastLdapUrlIndex;
         do{
             retryCount++;
             try{
                 Hashtable<Object, Object> env = new Hashtable<Object, Object>();
                 env.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY_CLASS);
                 env.put(Context.PROVIDER_URL, ldapServerUrls[currentLdapUrlIndex]);
                 env.put(Context.SECURITY_PRINCIPAL, username + "@" + domainName);
                 env.put(Context.SECURITY_CREDENTIALS, password);
                 InitialDirContext context = new InitialDirContext(env);
                 String filter = String.format(SEARCH_BY_SAM_ACCOUNT_NAME, username);
                 SearchControls constraints = new SearchControls();
                 constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
                 constraints.setReturningAttributes(attrIdsToSearch);
                 String[] dcComponents = domainName.split("\\.");
                 String userBase = "dc=" + dcComponents[0] + ",dc=" + dcComponents[1];
                 NamingEnumeration results = context.search(userBase, filter,constraints);
                 // Fail if no entries found
                 if(results == null || !results.hasMore()){
                     return false;
                 }

                 // Get result for the first entry found
                 SearchResult result = (SearchResult) results.next();

                 // Get the entry's distinguished name
                 NameParser parser = context.getNameParser("");
                 Name contextName = parser.parse(context.getNameInNamespace());
                 Name baseName = parser.parse(userBase);

                 Name entryName = parser.parse(new CompositeName(result.getName()).get(0));

                 // Get the entry's attributes
                 Attributes attrs = result.getAttributes();
                 Attribute attr = attrs.get(attrIdsToSearch[0]);

                 NamingEnumeration e = attr.getAll();
                 while(e.hasMore()){
                     String value = (String) e.next();
                     if(value.contains("GGH Admins") || value.contains("SSMH Admins") || value.contains("Elevated Users")){
                    	 return true;
                     }
                 }
                 lastLdapUrlIndex = currentLdapUrlIndex;
             }catch(CommunicationException exp){
                 // if the exception of type communication we can assume the AD is not reachable hence retry can be attempted with next available AD
                 if(retryCount < ldapServerUrls.length){
                     currentLdapUrlIndex++;
                     if(currentLdapUrlIndex == ldapServerUrls.length){
                         currentLdapUrlIndex = 0;
                     }
                     continue;
                 }
                 return false;
             }catch(AuthenticationException exp){
            	 return false;
             }catch(Throwable throwable){
                 return false;
             }
         }while(true);

        
    }
}
