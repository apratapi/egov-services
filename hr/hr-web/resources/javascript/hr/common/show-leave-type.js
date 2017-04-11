class ShowLeaveType extends React.Component {
  constructor(props) {
    super(props);
    this.state={leaveList:[],leaveType:{
    name:"",
    payEligible:"",
    encashable:"",
    halfdayAllowed:"",
    accumulative:"",
    description:"",
    active:""
  }}
  this.handleChange=this.handleChange.bind(this);
}

componentWillMount()
{
  //call employee api and get leaveList
  console.log(getUrlVars()["type"]);
//  var queryParam=getUrlVars();
}

componentDidMount()
{
  this.setState({
  leaveList:getCommonMaster("hr-leave","leavetypes","LeaveType").responseJSON["LeaveType"]
    });
}



componentDidUpdate(prevProps, prevState)
{
    if (prevState.leaveList.length!=this.state.leaveList.length) {
        // $('#leaveTable').DataTable().draw();
        // alert(prevState.leaveList.length);
        // alert(this.state.leaveList.length);
        // alert('updated');
        $('#leaveTable').DataTable({
          dom: 'Bfrtip',
          buttons: [
                   'copy', 'csv', 'excel', 'pdf', 'print'
           ],
           ordering: false
        });
    }
}


handleChange(e,name)
{

    this.setState({
        leaveType:{
            ...this.state.leaveType,
            [name]:e.target.value
        }
    })

}

  render() {
    let  {name,description}=this.state.leaveType;
    let {leaveList}=this.state;

    const renderBody=function()
    {

      return leaveList.map((item,index)=>
      {
            return (<tr key={index}>
                    <td>{index+1}</td>
                    <td data-label="name">{item.name}</td>
                    <td data-label="description">{item.description}</td>
                    <td data-label="action">
                              {renderAction(getUrlVars()["type"],item.id)}
                    </td>
                </tr>
            );

      })
    }
  const  renderAction=function(type,id)
    {
      if(type==="update")
      {

        return(
          <a href={`app/hr/leavemaster/leave-type.html?id=${id}&type=${type}`} className="btn btn-default btn-action"><span className="glyphicon glyphicon-pencil"></span></a>)
      }

      else {
            return(
                    <a href={`app/hr/leavemaster/leave-type.html?id=${id}&type=${type}`} className="btn btn-default btn-action"><span className="glyphicon glyphicon-modal-window"></span></a>
                  )
                }
         }





     return (
      <div>
          <h3>{getUrlVars()["type"]} leave Type</h3>
                <table id="leaveTable" className="table table-bordered">
                  <thead>
                      <tr>
                          <th>SL No. </th>
                          <th>Leave Name </th>
                          <th>Description</th>
                          <th>Action</th>
                      </tr>
                  </thead>

                  <tbody>
                    {renderBody()}
                  </tbody>
              </table>
      </div>

    );
  }
}

ReactDOM.render(
  <ShowLeaveType />,
  document.getElementById('root')
);
