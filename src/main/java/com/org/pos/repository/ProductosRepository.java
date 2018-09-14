package com.org.pos.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mysql.jdbc.Connection;
import com.org.pos.model.Usuario;
import com.org.pos.repository.UserRepository.UsuarioRowMapper;

@Repository
public class ProductosRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepository.class);

    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("exchangeDS")
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    @Transactional(readOnly=true)
    public Usuario findByUsername(String usuario) {
    	Usuario us = null;
    	try {
    		us = jdbcTemplate.queryForObject("SELECT * FROM usuarios WHERE email = ?", 
        			new Object[]{usuario}, new UsuarioRowMapper());    		
    	} catch(IncorrectResultSizeDataAccessException e) {
    		LOGGER.error("Invalid user");
    	}

    	return us;
    }
    
    
    /**
     * Obtiene lasumatoria de los dolares en depositos de banco que no estan bloqueados, validados o cancelados
     */
    @Transactional(readOnly=true)
    public Map<String, String> getBankFlow() throws Exception{
        try {
        	String query = "SELECT sum(dep.dolares) as total " + 
        			" FROM public.deposito as dep ";
        	
        	//List<Map<String, Object>> list = 
        	BigDecimal sumatoria=jdbcTemplate.queryForObject(query, BigDecimal.class);
        	Map<String, String> map = new HashMap<String, String>();
        	map.put("totalBalanceDlls", sumatoria.toString());
        	
        	return map;
        }catch (Exception e){
        	LOGGER.error("Error", e);
            throw e;
        }
    }
    
    /**
     * Inserta un registro
     * @param idDepo
     * @param status
     * @param idUser
     * @return
     * @throws Exception
     */
    public Integer insertHistoricDeposit(Long idDepo, Integer status, Long idUser) throws Exception{
        try {
        	String query = "INSERT INTO public.deposito_historico(id_deposito, status, id_usuario, fecha) "+
    		"VALUES (?, ?, ?, ?);";
        	
        	Integer result = jdbcTemplate.update(query, new Object[] {idDepo, status, idUser, new Timestamp(new Date().getTime())});
        	
        	return result;
        }catch (Exception e){
        	LOGGER.error("Error", e);
            throw e;
        }
    }
    
    /**
     * Actualizacion del deposito
     * @param idDepo
     * @param status
     * @param observaciones
     * @param folioAuditoria
     * @param grillaBnc
     * @return
     * @throws Exception
     */
    public Integer updateDeposit(Long idDepo, Integer status, String observaciones, String folioAuditoria, String grillaBnc) throws Exception{
        try {
        	String query = "UPDATE public.deposito\n" + 
        			"	SET status=?, grilla_bancaria=?, observaciones=?, folio_auditoria=?\n" + 
        			"	WHERE id = ?;";
        	
        	Integer result = jdbcTemplate.update(query, new Object[] {status, grillaBnc, observaciones, folioAuditoria, idDepo});
        	
        	return result;
        }catch (Exception e){
        	LOGGER.error("Error", e);
            throw e;
        }
    }
    
    public void marcarProductoComoUtilizadoEnVenta(String idProd,String cantidadAlmacen,String cantidadVendida, String catidadVendidaAlMomento){
        ///se descuenta del total de unidades y se incrementa el numero de vendidos
        
        int cantidadAl=Integer.parseInt(cantidadAlmacen);
        int cantidadVe=Integer.parseInt(cantidadVendida);
        int cantidadVendidaAlMomento=Integer.parseInt(catidadVendidaAlMomento);
        //DBConect conexion=new DBConect();  
        
        try{

          //Connection conexionMysql = null;
          //conexion.GetConnection();

          //Statement statement = conexionMysql.createStatement();
          
          String sqlString="Update productos set unidadesEnCaja="+ (cantidadAl-cantidadVendidaAlMomento)+
                           " , cantidadVendidos="+(cantidadVe+cantidadVendidaAlMomento)+
                           " where idProductos='"+idProd+"'";
          
          //statement.executeUpdate(sqlString);
          
          
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }

    private void agregarProductoActionPerformed(java.awt.event.ActionEvent evt) {                                                
        
        //DBConect conexion=new DBConect();
        String tipoProdSel="";//comboTipoProducto.getSelectedItem().toString();
        try{
            Connection conexionMysql = null;//conexion.GetConnection();

            Statement statement = conexionMysql.createStatement();

            String varDescripcion="";//descripcionAltaText.getText();
            String varUnidades="";//unidadesEnCajaText.getText();
            String varPrecioC="";//precioCompraAltaText.getText();
            String varPrecioV="";//precioVentaAltaText.getText();
            String varUMedida="";//uMedidaLista.getSelectedItem().toString();
            String varPresentacion="";//presentacionLista.getSelectedItem().toString();
            String varCodigo="";//codigoAltaText.getText();
            
            String precioChica="";//pChica.getText();
            String precioMediana="";//pMediana.getText();
            String precioGrande="";//pGrande.getText();
            String precioFamiliar="";//pFamiliar.getText();
            
            String tipoProd="";
            //0 general
            //1 pizza
            // tacos
            ////en el combo... Pizza, Tacos, General
            
            if(tipoProdSel.equals("---")){
                tipoProd="0";
            }
            if(tipoProdSel.equals("Pizza")){
                tipoProd="1";
                varPrecioC="0";
                varPrecioV="0";
                
            }
            if(tipoProdSel.equals("Tacos")){
                tipoProd="2";
            }
            if(tipoProdSel.equals("General")){
                tipoProd="0";
                precioChica="0";
                precioMediana="0";
                precioGrande="0";
                precioFamiliar="0";
            }
            
            
            String validadorVacios=varDescripcion+varUnidades+varPrecioC+varPrecioV+varUMedida+varPresentacion+varCodigo;
            
            if(validadorVacios.equals("")){
                
                //JOptionPane.showMessageDialog(null,"No has proporcionado ningun dato");
                
            }
            
            String mensajeError="";
            
            
//            if(varCodigo.equals("")){
//                
//                mensajeError+="Código \n";
//                
//            }
            
            if(varDescripcion.equals("")){
                
                mensajeError+="Descripción \n";
                
            }
            String seleccion="";//comboTipoProducto.getSelectedItem().toString();
            
            if(seleccion.equals("Pizza")){
                varUnidades="1";
                
            }
            
             if(varUnidades.equals("")){
                
                mensajeError+="Cantidad \n";
                
            }
             if(varPrecioC.equals("")){
                
                mensajeError+="Precio de compra \n";
                
            }
              if(varPrecioV.equals("")){
                
                mensajeError+="Precio de venta \n";
                
            }
            
            if(!mensajeError.equals("")){
            
                mensajeError="Los siguientes campos son necesarios para continuar: \n\n "+mensajeError;
                
                
                //JOptionPane.showMessageDialog(null, mensajeError);
                return; 
            }
             
            String sqlString="INSERT INTO `productos` (`descripcion`, `unidadesEnCaja`, `precioUnitarioC`, `uMedida`, `presentacion`, `cantidadFraccion`, `codigo`,`precioUnitarioV`,`TipoProducto`,precioChica,precioMediana,precioGrande,precioFamiliar) "
                    + " VALUES ('"+varDescripcion+"', '"+varUnidades+"', '"+varPrecioC+"', '"+varUMedida+"', '"+varPresentacion+"', '0', '"+varCodigo+"', '"+varPrecioV+"',"+tipoProd+","+precioChica+","+precioMediana+","+precioGrande+","+precioFamiliar+")";
        
            int resultado=statement.executeUpdate(sqlString);
       
            if(resultado>0){

                //JOptionPane.showMessageDialog(null, "El producto se agrego correctamente");
                if(tipoProd.equals("1")){
                   // refrescarComboAlAgregarProducto();
                }
                
                //limpiaNuevoProducto();
            }
            
       }catch(Exception e){
           e.printStackTrace();
       }
         
    }   
    private void BusquedaProductosTodoActionPerformed(java.awt.event.ActionEvent evt) {                                                      
        
        //DBConect conexion=new DBConect();
        //productosEncontradosAModificar= new DefaultTableModel();
        //listaProductosAModificar.setModel(productosEncontradosAModificar);
        
//        String code=codigoProdFiltro.getText();
        String desc="";//descripcionProdFiltro.getText();
        
        String filter=desc;
        
        if(filter.equals("")){
            //JOptionPane.showMessageDialog(null, "Se necesita al menos un dato para realizar la busqueda de productos");
            return;
        }
        
        try{

          Connection conexionMysql = null;//conexion.GetConnection();

          Statement statement = conexionMysql.createStatement();

          //String codigoVar=codigoProdFiltro.getText();
          String descripcionVar="";//descripcionProdFiltro.getText();
          
          String sqlString="Select idProductos as 'Id',codigo,descripcion as 'Descripción',precioUnitarioC as 'Precio compra', precioUnitarioV as 'Precio venta',unidadesEnCaja as 'Cantidad'"
                  + ",uMedida as 'Unidad medida', presentacion as 'Presentación' from  productos " +
                           " where estatus=0 ";
                           
          if(!descripcionVar.equals("")){
               sqlString+= " And descripcion like '%"+descripcionVar+"%'";
          }
          
//          if(codigoVar.equals("") && !descripcionVar.equals("")){
//              
//              sqlString="SELECT idProductos as 'Id',codigo,descripcion as 'Descripción',precioUnitarioC as 'Precio compra', precioUnitarioV as 'Precio venta',unidadesEnCaja as 'Cantidad' "
//                      +",uMedida as 'Unidad medida', presentacion as 'Presentación'"
//                      + " FROM productos where estatus=0 and descripcion like '%"+descripcionVar+"%'";
//              
//          }
          
            ResultSet rs = statement.executeQuery(sqlString); 
            int contador=0;  
     
            ResultSetMetaData rsMd = rs.getMetaData();
            //La cantidad de columnas que tiene la consulta
            int cantidadColumnas = rsMd.getColumnCount();
            //Establecer como cabezeras el nombre de las colimnas
            for (int i = 1; i <= cantidadColumnas; i++) {
             //productosEncontradosAModificar.addColumn(rsMd.getColumnLabel(i));
            }
            //Creando las filas para el JTable
            while (rs.next()) {
             contador++;
             Object[] fila = new Object[cantidadColumnas];
             for (int i = 0; i < cantidadColumnas; i++) {
               fila[i]=rs.getObject(i+1);
             }
             //productosEncontradosAModificar.addRow(fila);
            }
          
            if(contador==0){
                //JOptionPane.showMessageDialog(null, "No se encontro ningun producto con los datos proporcionados.");
            }
            
            
        }catch(Exception e){
            e.printStackTrace();
        }
    } 
    private void ModificarProductosGuardarActionPerformed(java.awt.event.ActionEvent evt) {                                                          
        int fila=0;//listaProductosAModificar.getSelectedRowCount();
        
        if(fila==0){
            //JOptionPane.showMessageDialog(null,"No has seleccionado un producto para modificar");
            return;
        }else{
            fila=fila-1;
        }
        fila=0;//listaProductosAModificar.getSelectedRow();
        String idProductoModificar="";//+listaProductosAModificar.getValueAt(fila, 0);
        String codigo="";//+listaProductosAModificar.getValueAt(fila, 1);
        //DBConect conexion=new DBConect();  
        
        try{

          Connection conexionMysql =null;// conexion.GetConnection();

          Statement statement = conexionMysql.createStatement();
          String descripcionVar="";//descripcionAltaText1.getText();
          String precioCVar="";//precioCompraAltaText1.getText();
          String precioVVar="";//precioVentaAltaText1.getText();
          String cantidad="";//unidadesEnCajaText1.getText();
          String uM="";//uMedidaLista1.getSelectedItem().toString();
          String presentacion="";//presentacionLista1.getSelectedItem().toString();

          String sqlString="UPDATE productos set descripcion='"+descripcionVar+"',precioUnitarioC="+precioCVar+", "
                            + " precioUnitarioV="+precioVVar+",unidadesEnCaja="+cantidad+",uMedida='"+uM+"', presentacion='"+presentacion+"'"
                            +" where idProductos='"+idProductoModificar+"'";
          
          int resultado=statement.executeUpdate(sqlString);
          
          if(resultado>=1){
                //JOptionPane.showMessageDialog(null, "Se modifico correctamente la informacion del producto");
                //BusquedaProductosTodoActionPerformed(evt);
                //descripcionAltaText1.setText("");
                //precioVentaAltaText1.setText("");
                //precioCompraAltaText1.setText("");
                //unidadesEnCajaText1.setText("");

                //uMedidaLista1.setSelectedItem("---");
                //presentacionLista1.setSelectedItem("---");
          }else{
              //JOptionPane.showMessageDialog(null, "Ocurrio un error al guardar los cambios por favor intente nuevamente");
          }
          
          //codigoProdFiltro.setText("");
          //descripcionProdFiltro.setText("");
          //panelModificarProducto.setVisible(false);
          
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
    private void removerProductoActionPerformed(java.awt.event.ActionEvent evt) {                                                
        int fila=0;//listaProductosAModificar.getSelectedRowCount();
        
        if(fila==0){
            //JOptionPane.showMessageDialog(null,"No has seleccionado un producto para desactivar");
            return;
        }else{
            fila=fila-1;
        }
        
        int confirmacion= 0;
       if(confirmacion==0){
            String idProductoModificar="";//+listaProductosAModificar.getValueAt(fila, 0);
        
            //DBConect conexion=new DBConect();  

            try{

              Connection conexionMysql = null;//conexion.GetConnection();

              Statement statement = conexionMysql.createStatement();

              String sqlString="UPDATE productos set activo=0"
                                +" where idProductos='"+idProductoModificar+"'";

              int resultado=statement.executeUpdate(sqlString);

              if(resultado>=1){
                    //JOptionPane.showMessageDialog(null, "Se desactivo correctamente la informacion del producto");
              }else{
                  //JOptionPane.showMessageDialog(null, "Ocurrio un error al desactivar el producto por favor intente nuevamente");
              }

              //codigoProdFiltro.setText("");
              //descripcionProdFiltro.setText("");
              //panelModificarProducto.setVisible(true);

            }catch(Exception e){
                e.printStackTrace();
            }
       }
        
    }                                             
	private void busquedaManualActionPerformed(java.awt.event.ActionEvent evt) {                                               
	        
	        //DBConect conexion=new DBConect();
	        try{
	
	          //Connection conexionMysql = conexion.GetConnection();
	
	          Statement statement = null;//conexionMysql.createStatement();
	
	          String codigoVar="";//codigoManual.getText();
	          String descripcionVar="";//descripcionManual.getText();
	          
	          String filter=codigoVar+descripcionVar;
	        
	          if(filter.equals("")){
	            //JOptionPane.showMessageDialog(null, "Se necesita al menos un dato para realizar la busqueda de productos");
	            return;
	          }
	          
	          String sqlString="Select idProductos as 'Id',codigo,descripcion as 'Descripción',precioUnitarioC as 'Precio compra', precioUnitarioV as 'Precio venta',unidadesEnCaja as 'Cantidad'"
	                  + ",uMedida as 'Unidad medida', presentacion as 'Presentación' from  productos " +
	                           " where estatus=0 and codigo='"+codigoVar+"'";
	                           
	          if(!descripcionVar.equals("")){
	               sqlString+= " And descripcion like '%"+descripcionVar+"%'";
	          }
	          
	          if(codigoVar.equals("") && !descripcionVar.equals("")){
	              
	              sqlString="SELECT idProductos as 'Id',descripcion as 'Descripción',precioUnitarioV as 'Precio venta',unidadesEnCaja as 'Cantidad' "
	                      +",uMedida as 'Unidad medida', presentacion as 'Presentación'"
	                      + " FROM productos where estatus=0 and descripcion like '%"+descripcionVar+"%'";
	              
	          }
	          
	            ResultSet rs = statement.executeQuery(sqlString); 
	            int contador=0;  
	            
	            while (rs.next()) {
	             contador++;
	            }
	            rs.beforeFirst();
	            if(contador>=1){
	                
	               
	                //DefaultTableModel productosEncontrados= new DefaultTableModel();
	                
	                //ResultSetMetaData rsMd = rs.getMetaData();
	                //La cantidad de columnas que tiene la consulta
	                //int cantidadColumnas = rsMd.getColumnCount();
	                //Establecer como cabezeras el nombre de las colimnas
	                //for (int i = 1; i <= cantidadColumnas; i++) {
	                 //productosEncontrados.addColumn(rsMd.getColumnLabel(i));
	                //}
	                //Creando las filas para el JTable
	                //while (rs.next()) {
	                 //Object[] fila = new Object[cantidadColumnas];
	                 //for (int i = 0; i < cantidadColumnas; i++) {
	                 //  fila[i]=rs.getObject(i+1);
	                 //}
	                 //productosEncontrados.addRow(fila);
	                //}
	                
	                
	                //BuscadorProductos buscador=new BuscadorProductos(productosEncontrados,this);
	                //buscador.setTitle("Buscador de productos");
	                //buscador.setVisible(true);
	                //buscador.toFront();
	                
	            }else{
	                
	            	//JOptionPane.showMessageDialog(null,"No se encontraron resultados en búsqueda manual");
	                return;
	            }
	            
	        }catch(Exception e){
	            e.printStackTrace();
	        } 
	    }                                          
	
    class UsuarioRowMapper implements RowMapper<Usuario> {
        @Override
        public Usuario mapRow(ResultSet rs, int rowNum) throws SQLException {
        	Usuario usuario = new Usuario();

        	usuario.setId(rs.getInt("idusuario"));
        	usuario.setNombre(rs.getString("nombre"));
        	usuario.setApellidop(rs.getString("apellidop"));
        	usuario.setApellidom(rs.getString("apellidom"));
        	usuario.setNombreUsuario(rs.getString("nombreUsuario"));
        	usuario.setPassword(rs.getString("password"));
        	usuario.setIdRol(rs.getInt("rol_idrol"));
        	//usuario.setCorreo(rs.getString("correo"));
        	//usuario.setTelefono(rs.getString("telefono"));
        	//usuario.setId_rol(rs.getInt("id_rol"));
        	//usuario.setId_supervisor(rs.getInt("id_supervisor"));
            return usuario;
        }
        
    }
	
}
