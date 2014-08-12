import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.CommunicationException;

public class MemberOfAuth{

	private final String domainName;
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
    
    public boolean isMemberOf(String group, String user, String pass){
    	Hashtable<String, String> env = new Hashtable<String, String>();
    	env.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY_CLASS);
    	env.put(Context.PROVIDER_URL, ldapServerUrls[lastLdapUrlIndex]);
    	env.put(Context.SECURITY_PRINCIPAL, user + "@" + domainName);
    	env.put(Context.SECURITY_CREDENTIALS, pass);
    	 
    	DirContext ctx;
    	int retryCount = 0;
    	int currentLdapUrlIndex = lastLdapUrlIndex;
    	do{
    		if(retryCount > 0){
    			env.remove(Context.PROVIDER_URL);
    			env.put(Context.PROVIDER_URL, ldapServerUrls[currentLdapUrlIndex]);
    		}
    		retryCount++;
    	try {
    	    //Authenticate the logon user
    	    ctx = new InitialDirContext(env);
    	    String searchBase = "DC=FLH,DC=LOCAL";
    	    
    	    // Perform an exact group match with the "memberOf" attribute.
    	    StringBuilder searchFilter = new StringBuilder("(&");
    	    searchFilter.append("(objectClass=person)");
    	    searchFilter.append("(sAMAccountName="+user+")");
    	    searchFilter.append("(memberOf=CN=GGH Admins,OU=Admin Accounts,OU=Information Services,OU=GGH,DC=FLH,DC=LOCAL)"); // Add code to query memberOf 
    	    searchFilter.append(")");																						// from GGH Admin group so its not hard coded
    	    SearchControls sCtrl = new SearchControls();
    	    sCtrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
    	 
    	    NamingEnumeration<SearchResult> answer = ctx.search(searchBase, searchFilter.toString(), sCtrl);
    	    boolean passes = false;
    	 
    	    if (answer.hasMoreElements()) {
    	        passes = true;
    	    }
    	 
    	    if (passes) {
    	        // The user belongs to the group. Do something...
    	    	return true;
    	    }
    	} catch(CommunicationException exp){
            // if the exception of type communication we can assume the AD is not reachable hence retry can be attempted with next available AD
            currentLdapUrlIndex++;
        }catch(AuthenticationException exp){
       	 	return false;
        }catch(Throwable throwable){
        	return false;
        }
    	}while(retryCount < ldapServerUrls.length);
    	
    	return false;
    }
    public SearchResult findAccountByAccountName(DirContext ctx, String ldapSearchBase, String accountName) throws NamingException {

        String searchFilter = "(&(objectClass=user)(sAMAccountName=" + accountName + "))";
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, searchFilter, searchControls);
        
        SearchResult searchResult = null;
        if(results.hasMoreElements()) {
             searchResult = (SearchResult) results.nextElement();

            //make sure there is not another item available, there should be only 1 match
            if(results.hasMoreElements()) {
                System.err.println("Matched multiple users for the accountName: " + accountName);
                return null;
            }
        }
        
        return searchResult;
    }

}
